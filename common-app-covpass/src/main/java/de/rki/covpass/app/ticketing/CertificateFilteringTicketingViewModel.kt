/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.ticketing

import android.os.Parcelable
import android.util.Base64
import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.http.httpConfig
import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import de.rki.covpass.sdk.dependencies.defaultJson
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.storage.CertRepository
import de.rki.covpass.sdk.ticketing.*
import de.rki.covpass.sdk.ticketing.TicketingType.Companion.valueOfOrNull
import de.rki.covpass.sdk.ticketing.data.accesstoken.TicketingAccessTokenRequest
import de.rki.covpass.sdk.ticketing.data.accesstoken.TicketingAccessTokenResponse
import de.rki.covpass.sdk.ticketing.data.accesstoken.TicketingAccessTokenResponseContainer
import de.rki.covpass.sdk.ticketing.data.identity.TicketingIdentityDocument
import de.rki.covpass.sdk.ticketing.data.identity.TicketingServiceRemote
import de.rki.covpass.sdk.ticketing.data.identity.TicketingValidationServiceIdentityResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import java.security.KeyPair
import java.security.KeyPairGenerator

public interface CertificateFilteringEvents : TicketingCancellationEvents {
    public fun onFilteringCompleted(
        list: List<CombinedCovCertificate>,
        encryptionData: BookingPortalEncryptionData,
    )

    public fun onEmptyList()
    public fun showUserData(
        firstName: String,
        lastName: String,
        dob: String,
        types: List<TicketingType>,
    )
}

public interface TicketingCancellationEvents : BaseEvents {
    public fun onCancelled()
}

@Parcelize
public data class ValidationTicketingTestObject(
    val qrString: String,
    val keyPair: KeyPair,
    val jwtToken: String,
    val validationServiceIdentityResponse: TicketingValidationServiceIdentityResponse,
    val iv: String,
    val accessTokenValidationUrl: String,
    val validationServiceId: String,
    val cancellationServiceUrl: String?,
) : Parcelable

public class CertificateFilteringTicketingViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val ticketingDataInitialization: TicketingDataInitialization,
    private val identityDocumentRepository: IdentityDocumentRepository =
        sdkDeps.identityDocumentRepository,
    private val accessTokenRepository: AccessTokenRepository =
        sdkDeps.accessTokenRepository,
    private val validationServiceIdentityRepository: ValidationServiceIdentityRepository =
        sdkDeps.validationServiceIdentityRepository,
    private val cancellationRepository: CancellationRepository =
        sdkDeps.cancellationRepository,
    private val certRepository: CertRepository =
        covpassDeps.certRepository,
) : BaseReactiveState<CertificateFilteringEvents>(scope) {

    private val state: MutableStateFlow<State> = MutableStateFlow(State.Initial)

    init {
        initialize()
    }

    private fun initialize() {
        launch {
            val keyPairGen = KeyPairGenerator.getInstance("EC")
            keyPairGen.initialize(256)
            val keyPair: KeyPair = keyPairGen.generateKeyPair()

            val ticketingIdentityDocument = fetchIdentityDocument(ticketingDataInitialization)
            val validationService = getTrustedValidationService(
                ticketingIdentityDocument.validationServices
            )
            state.value = State.Fetched(
                ticketingIdentityDocument.cancellationService.serviceEndpoint
            )
            val ticketingValidationServiceIdentity = fetchValidationServiceIdentity(
                validationService
            )
            val ticketingAccessTokenResponseContainer = fetchAccessToken(
                keyPair,
                ticketingDataInitialization,
                ticketingIdentityDocument.accessTokenService,
                validationService
            )
            val bookingPortalEncryptionData = BookingPortalEncryptionData(
                keyPair,
                ticketingAccessTokenResponseContainer,
                ticketingValidationServiceIdentity,
                validationService.id,
                getCancellationUrl()
            )

            filterCertificates(bookingPortalEncryptionData)
        }
    }

    private fun getTrustedValidationService(
        validationServices: List<TicketingServiceRemote>,
    ): TicketingServiceRemote {
        if (validationServices.isEmpty()) throw NoValidationServiceListed()
        return validationServices.find { httpConfig.hasPublicKey(it.serviceEndpoint) }
            ?: throw InvalidValidationServiceProvider(validationServices.first().name)
    }

    private fun filterCertificates(encryptionData: BookingPortalEncryptionData) {
        val certificateData = encryptionData.accessTokenContainer.accessToken.certificateData
        val filteredCerts = certRepository.certs.value.filterCertificates(
            certificateData.greenCertificateTypesTrimmed.mapNotNull { valueOfOrNull(it) },
            certificateData.standardizedGivenNameTrimmed,
            certificateData.standardizedFamilyNameTrimmed,
            certificateData.dateOfBirthTrimmed
        )

        eventNotifier {
            showUserData(
                certificateData.standardizedGivenNameTrimmed,
                certificateData.standardizedFamilyNameTrimmed,
                certificateData.dateOfBirthTrimmed,
                certificateData.greenCertificateTypesTrimmed.mapNotNull { valueOfOrNull(it) }
            )

            if (filteredCerts.isEmpty()) onEmptyList()
            else onFilteringCompleted(filteredCerts, encryptionData)
        }
    }

    private suspend fun fetchIdentityDocument(
        ticketingDataInitialization: TicketingDataInitialization,
    ): TicketingIdentityDocument =
        identityDocumentRepository.fetchIdentityDocument(ticketingDataInitialization)

    private suspend fun fetchAccessToken(
        keyPair: KeyPair,
        ticketingDataInitialization: TicketingDataInitialization,
        accessTokenService: TicketingServiceRemote,
        validationService: TicketingServiceRemote,
    ): TicketingAccessTokenResponseContainer {
        val accessTokenRequest = TicketingAccessTokenRequest(
            validationService.id,
            Base64.encodeToString(keyPair.public.encoded, Base64.NO_WRAP)
        )

        val ticketingAccessTokenData = accessTokenRepository.fetchAccessToken(
            accessTokenService.serviceEndpoint,
            ticketingDataInitialization.token,
            accessTokenRequest
        )
        val ticketingAccessTokenResponse: TicketingAccessTokenResponse =
            ticketingAccessTokenData.jwtToken.parseJwtToken().let {
                try {
                    defaultJson.decodeFromString(it.body)
                } catch (exception: SerializationException) {
                    throw AccessTokenDecodingException()
                }
            }
        return TicketingAccessTokenResponseContainer(
            ticketingAccessTokenResponse,
            ticketingAccessTokenData
        )
    }

    private suspend fun fetchValidationServiceIdentity(
        validationService: TicketingServiceRemote,
    ): TicketingValidationServiceIdentityResponse =
        validationServiceIdentityRepository.fetchValidationServiceIdentity(
            validationService.serviceEndpoint
        )

    public fun cancel(token: String) {
        launch {
            getCancellationUrl()?.let {
                cancellationRepository.cancelTicketing(it, token)
                eventNotifier { onCancelled() }
            }
        }
    }

    private fun getCancellationUrl(): String? {
        return when (val state = state.value) {
            is State.Fetched -> {
                state.cancellationServiceUrl
            }
            is State.Initial -> null
        }
    }

    private sealed interface State {
        object Initial : State
        data class Fetched(val cancellationServiceUrl: String) : State
    }
}

public class AccessTokenDecodingException : IllegalStateException()

public class InvalidValidationServiceProvider(public val validationService: String) : IllegalStateException()

public class NoValidationServiceListed : IllegalStateException()
