/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.ticketing

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.detail.DetailExportPdfFragment
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.TestCert
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.storage.CertRepository
import de.rki.covpass.sdk.ticketing.TicketingValidationRepository
import de.rki.covpass.sdk.ticketing.base64ToX509Certificate
import de.rki.covpass.sdk.ticketing.data.validate.BookingValidationResponse
import de.rki.covpass.sdk.ticketing.encoding.TicketingValidationRequestProvider
import kotlinx.coroutines.CoroutineScope
import java.security.PublicKey

public interface ValidationTicketingEvents : BaseEvents {
    public fun onValidationComplete(bookingValidationResponse: BookingValidationResponse)
    public fun onVaccinationResult(bookingValidationResponse: BookingValidationResponse)
    public fun onRecoveryResult(bookingValidationResponse: BookingValidationResponse)
    public fun onTestCertResult(bookingValidationResponse: BookingValidationResponse)
}

public class ValidateTicketingViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val ticketingValidationRepository: TicketingValidationRepository =
        sdkDeps.ticketingValidationRepository,
    private val ticketingValidationRequestProvider: TicketingValidationRequestProvider =
        sdkDeps.ticketingValidationRequestProvider,
    private val covPassCertRepository: CertRepository = covpassDeps.certRepository
) : BaseReactiveState<ValidationTicketingEvents>(scope) {

    public fun validate(validationTicketingTestObject: ValidationTicketingTestObject) {
        launch {
            val publicKeyJwk =
                validationTicketingTestObject.validationServiceIdentityResponse.getEncryptionPublicKey()
            val publicKey: PublicKey = publicKeyJwk.x5c.first().base64ToX509Certificate().publicKey

            val validationRequest =
                ticketingValidationRequestProvider.provideTicketValidationRequest(
                    validationTicketingTestObject.qrString,
                    publicKeyJwk.kid,
                    publicKey,
                    validationTicketingTestObject.iv,
                    validationTicketingTestObject.keyPair.private
                )

            val response = ticketingValidationRepository.fetchValidationResult(
                validationTicketingTestObject.accessTokenValidationUrl,
                validationTicketingTestObject.jwtToken,
                validationRequest
            )

            eventNotifier { onValidationComplete(response) }
        }
    }

    public fun showResult(certId: String, bookingValidationResponse: BookingValidationResponse) {
        val combinedCovCertificate = covPassCertRepository.certs.value.getCombinedCertificate(certId)
            ?: throw DetailExportPdfFragment.NullCertificateException()
        when (combinedCovCertificate.covCertificate.dgcEntry) {
            is Vaccination -> {
                eventNotifier { onVaccinationResult(bookingValidationResponse) }
            }
            is Recovery -> {
                eventNotifier { onRecoveryResult(bookingValidationResponse) }
            }
            is TestCert -> {
                eventNotifier { onTestCertResult(bookingValidationResponse) }
            }
        }
    }
}
