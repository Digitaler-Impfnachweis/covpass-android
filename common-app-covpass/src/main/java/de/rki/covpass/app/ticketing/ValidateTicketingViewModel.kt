/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.ticketing

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import de.rki.covpass.logging.Lumber
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.ticketing.CancellationRepository
import de.rki.covpass.sdk.ticketing.TicketingValidationRepository
import de.rki.covpass.sdk.ticketing.base64ToX509Certificate
import de.rki.covpass.sdk.ticketing.data.validate.BookingValidationResponse
import de.rki.covpass.sdk.ticketing.encoding.TicketingValidationRequestProvider
import kotlinx.coroutines.CoroutineScope
import java.security.PublicKey

public interface ValidationTicketingEvents : TicketingCancellationEvents {
    public fun onValidationComplete(bookingValidationResponse: BookingValidationResponse)
    public fun onResult(bookingValidationResponse: BookingValidationResponse)
}

public class ValidateTicketingViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val ticketingValidationRepository: TicketingValidationRepository =
        sdkDeps.ticketingValidationRepository,
    private val ticketingValidationRequestProvider: TicketingValidationRequestProvider =
        sdkDeps.ticketingValidationRequestProvider,
    private val cancellationRepository: CancellationRepository =
        sdkDeps.cancellationRepository,
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

    public fun showResult(bookingValidationResponse: BookingValidationResponse) {
        eventNotifier { onResult(bookingValidationResponse) }
    }

    public fun cancel(url: String?, token: String) {
        launch {
            try {
                url?.let {
                    cancellationRepository.cancelTicketing(url, token)
                }
                eventNotifier { onCancelled() }
            } catch (e: Exception) {
                Lumber.e(e)
                eventNotifier { onCancelled() }
            }
        }
    }
}
