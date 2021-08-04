/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.scanner

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.ErrorEvents
import de.rki.covpass.checkapp.validitycheck.validate
import de.rki.covpass.logging.Lumber
import de.rki.covpass.sdk.cert.QRCoder
import de.rki.covpass.sdk.cert.RulesValidator
import de.rki.covpass.sdk.cert.models.*
import de.rki.covpass.sdk.cert.validateEntity
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.utils.isValid
import kotlinx.coroutines.CoroutineScope
import java.time.ZonedDateTime

/**
 * Interface to communicate events from [CovPassCheckQRScannerViewModel] to [CovPassCheckQRScannerFragment].
 */
internal interface CovPassCheckQRScannerEvents : ErrorEvents {
    fun onValidationSuccess(certificate: CovCertificate)
    fun onValidationFailure()
    fun onValidPcrTest(certificate: CovCertificate, sampleCollection: ZonedDateTime?)
    fun onValidAntigenTest(certificate: CovCertificate, sampleCollection: ZonedDateTime?)
}

/**
 * ViewModel holding the business logic for decoding and validating a [CovCertificate].
 */
internal class CovPassCheckQRScannerViewModel(
    scope: CoroutineScope,
    private val qrCoder: QRCoder = sdkDeps.qrCoder,
    private val rulesValidator: RulesValidator = sdkDeps.rulesValidator
) : BaseReactiveState<CovPassCheckQRScannerEvents>(scope) {

    fun onQrContentReceived(qrContent: String) {
        launch {
            try {
                val covCertificate = qrCoder.decodeCovCert(qrContent)
                val dgcEntry = covCertificate.dgcEntry
                validateEntity(dgcEntry.idWithoutPrefix)
                validate(covCertificate, rulesValidator)
                when (dgcEntry) {
                    is Vaccination -> {
                        when (dgcEntry.type) {
                            VaccinationCertType.VACCINATION_FULL_PROTECTION -> {
                                eventNotifier {
                                    onValidationSuccess(covCertificate)
                                }
                            }
                            else -> {
                                eventNotifier {
                                    onValidationFailure()
                                }
                            }
                        }
                    }
                    is Test -> {
                        when (dgcEntry.type) {
                            TestCertType.NEGATIVE_PCR_TEST -> {
                                handleNegativePcrResult(covCertificate)
                            }
                            TestCertType.POSITIVE_PCR_TEST -> {
                                eventNotifier { onValidationFailure() }
                            }
                            TestCertType.NEGATIVE_ANTIGEN_TEST -> {
                                handleNegativeAntigenResult(covCertificate)
                            }
                            TestCertType.POSITIVE_ANTIGEN_TEST -> {
                                eventNotifier { onValidationFailure() }
                            }
                            // .let{} to enforce exhaustiveness
                        }.let {}
                    }
                    is Recovery -> {
                        if (isValid(dgcEntry.validFrom, dgcEntry.validUntil)) {
                            eventNotifier { onValidationSuccess(covCertificate) }
                        } else {
                            eventNotifier { onValidationFailure() }
                        }
                    }
                    // .let{} to enforce exhaustiveness
                }.let {}
            } catch (exception: Exception) {
                Lumber.e(exception)
                eventNotifier { onValidationFailure() }
            }
        }
    }

    private fun handleNegativePcrResult(
        covCertificate: CovCertificate
    ) {
        val test = covCertificate.dgcEntry as Test
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
        val test = covCertificate.dgcEntry as Test
        eventNotifier {
            onValidAntigenTest(
                covCertificate,
                test.sampleCollection
            )
        }
    }
}
