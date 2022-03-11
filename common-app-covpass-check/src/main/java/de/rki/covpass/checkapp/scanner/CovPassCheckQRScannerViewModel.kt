/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.scanner

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ensody.reactivestate.ErrorEvents
import de.rki.covpass.checkapp.validitycheck.CovPassCheckValidationResult
import de.rki.covpass.checkapp.validitycheck.validate
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.storage.CheckContextRepository
import de.rki.covpass.logging.Lumber
import de.rki.covpass.sdk.cert.CovPassRulesValidator
import de.rki.covpass.sdk.cert.QRCoder
import de.rki.covpass.sdk.cert.models.*
import de.rki.covpass.sdk.cert.validateEntity
import de.rki.covpass.sdk.dependencies.sdkDeps
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
    private val checkContextRepository: CheckContextRepository = commonDeps.checkContextRepository,
) : BaseReactiveState<CovPassCheckQRScannerEvents>(scope) {

    fun onQrContentReceived(qrContent: String) {
        launch {
            try {
                val covCertificate = qrCoder.decodeCovCert(
                    qrContent,
                    checkContextRepository.isExpertModeOn.value
                )
                val dgcEntry = covCertificate.dgcEntry
                validateEntity(dgcEntry.idWithoutPrefix)
                when (validate(covCertificate, getRuleValidator())) {
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
        covCertificate: CovCertificate
    ) {
        val test = covCertificate.dgcEntry as TestCert
        eventNotifier {
            onValidPcrTest(
                covCertificate,
                test.sampleCollection
            )
        }
    }

    private fun handleNegativeAntigenResult(
        covCertificate: CovCertificate
    ) {
        val test = covCertificate.dgcEntry as TestCert
        eventNotifier {
            onValidAntigenTest(
                covCertificate,
                test.sampleCollection
            )
        }
    }
}
