/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.scanner

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ensody.reactivestate.ErrorEvents
import com.ensody.reactivestate.MutableValueFlow
import de.rki.covpass.checkapp.validitycheck.CovPassCheckValidationResult
import de.rki.covpass.checkapp.validitycheck.validate
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.storage.CheckContextRepository
import de.rki.covpass.logging.Lumber
import de.rki.covpass.sdk.cert.CovPassRulesValidator
import de.rki.covpass.sdk.cert.QRCoder
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.TestCert
import de.rki.covpass.sdk.cert.models.TestCertType
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.cert.validateEntity
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.revocation.RevocationRemoteListRepository
import kotlinx.coroutines.CoroutineScope
import java.time.ZonedDateTime

/**
 * Interface to communicate events from [CovPassCheckQRScannerViewModel] to [CovPassCheckQRScannerFragment].
 */
internal interface CovPassCheckQRScannerEvents : ErrorEvents {
    fun onValidationSuccess(certificate: CovCertificate)
    fun onValidationFailure(isTechnical: Boolean = false, certificate: CovCertificate? = null)
    fun onValidPcrTest(certificate: CovCertificate, sampleCollection: ZonedDateTime?)
    fun onValidAntigenTest(certificate: CovCertificate, sampleCollection: ZonedDateTime?)
}

/**
 * ViewModel holding the business logic for decoding and validating a [CovCertificate].
 */
internal class CovPassCheckQRScannerViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val qrCoder: QRCoder = sdkDeps.qrCoder,
    private val euRulesValidator: CovPassRulesValidator = sdkDeps.euRulesValidator,
    private val domesticRulesValidator: CovPassRulesValidator = sdkDeps.domesticRulesValidator,
    private val revocationRemoteListRepository: RevocationRemoteListRepository = sdkDeps.revocationRemoteListRepository,
    private val checkContextRepository: CheckContextRepository = commonDeps.checkContextRepository,
) : BaseReactiveState<CovPassCheckQRScannerEvents>(scope) {

    val recoveryOlder90DaysValid: MutableValueFlow<Boolean> = MutableValueFlow(true)

    fun onQrContentReceived(qrContent: String) {
        launch {
            try {
                val covCertificate = qrCoder.decodeCovCert(qrContent)
                val dgcEntry = covCertificate.dgcEntry
                validateEntity(dgcEntry.idWithoutPrefix)
                when (
                    validate(
                        covCertificate,
                        getRuleValidator(),
                        revocationRemoteListRepository,
                        recoveryOlder90DaysValid.value,
                    )
                ) {
                    CovPassCheckValidationResult.Success -> {
                        when (dgcEntry) {
                            is Vaccination, is Recovery -> {
                                eventNotifier {
                                    onValidationSuccess(covCertificate)
                                }
                            }
                            is TestCert -> {
                                if (dgcEntry.type == TestCertType.NEGATIVE_PCR_TEST) {
                                    handleNegativePcrResult(covCertificate)
                                } else {
                                    handleNegativeAntigenResult(covCertificate)
                                }
                            }
                            // .let{} to enforce exhaustiveness
                        }.let {}
                    }
                    CovPassCheckValidationResult.TechnicalError -> eventNotifier {
                        onValidationFailure(true, covCertificate)
                    }
                    CovPassCheckValidationResult.ValidationError -> eventNotifier {
                        onValidationFailure(certificate = covCertificate)
                    }
                }
            } catch (exception: Exception) {
                Lumber.e(exception)
                eventNotifier { onValidationFailure(true) }
            }
        }
    }

    private fun getRuleValidator(): CovPassRulesValidator =
        if (checkContextRepository.isDomesticRulesOn.value) {
            domesticRulesValidator
        } else {
            euRulesValidator
        }

    private fun handleNegativePcrResult(
        covCertificate: CovCertificate,
    ) {
        val test = covCertificate.dgcEntry as TestCert
        eventNotifier {
            onValidPcrTest(
                covCertificate,
                test.sampleCollection,
            )
        }
    }

    private fun handleNegativeAntigenResult(
        covCertificate: CovCertificate,
    ) {
        val test = covCertificate.dgcEntry as TestCert
        eventNotifier {
            onValidAntigenTest(
                covCertificate,
                test.sampleCollection,
            )
        }
    }
}
