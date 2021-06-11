/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.scanner

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.ErrorEvents
import de.rki.covpass.logging.Lumber
import de.rki.covpass.sdk.cert.BadCoseSignatureException
import de.rki.covpass.sdk.cert.ExpiredCwtException
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.Test
import de.rki.covpass.sdk.cert.models.TestCertType
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.cert.models.VaccinationCertType
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.utils.isOlderThan
import de.rki.covpass.sdk.utils.isValid
import kotlinx.coroutines.CoroutineScope
import java.time.ZonedDateTime

/**
 * Interface to communicate events from [CovPassCheckQRScannerViewModel] to [CovPassCheckQRScannerFragment].
 */
internal interface CovPassCheckQRScannerEvents : ErrorEvents {
    fun onValidationSuccess(certificate: CovCertificate)
    fun onValidationFailure()

    // Test - PCR
    fun onNegativeValidPcrTest(certificate: CovCertificate, sampleCollection: ZonedDateTime?)
    fun onNegativeExpiredPcrTest(certificate: CovCertificate, sampleCollection: ZonedDateTime?)

    // Test - Antigen
    fun onNegativeValidAntigenTest(certificate: CovCertificate, sampleCollection: ZonedDateTime?)
    fun onNegativeExpiredAntigenTest(certificate: CovCertificate, sampleCollection: ZonedDateTime?)
}

/**
 * ViewModel holding the business logic for decoding and validating a [CovCertificate].
 */
internal class CovPassCheckQRScannerViewModel(scope: CoroutineScope) :
    BaseReactiveState<CovPassCheckQRScannerEvents>(scope) {

    fun onQrContentReceived(qrContent: String) {
        launch {
            try {
                val covCertificate = sdkDeps.qrCoder.decodeCovCert(qrContent)
                val dgcEntry = covCertificate.dgcEntry
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
                when (exception) {
                    is BadCoseSignatureException, is ExpiredCwtException -> {
                        Lumber.e(exception)
                        eventNotifier { onValidationFailure() }
                    }
                    else -> throw exception
                }
            }
        }
    }

    private fun handleNegativePcrResult(
        covCertificate: CovCertificate
    ) {
        val test = covCertificate.dgcEntry as Test
        val isOlder = test.sampleCollection?.isOlderThan(
            Test.PCR_TEST_EXPIRY_TIME_HOURS
        ) ?: false
        if (isOlder) {
            eventNotifier {
                onNegativeExpiredPcrTest(
                    covCertificate,
                    test.sampleCollection
                )
            }
        } else {
            eventNotifier {
                onNegativeValidPcrTest(
                    covCertificate,
                    test.sampleCollection
                )
            }
        }
    }

    private fun handleNegativeAntigenResult(
        covCertificate: CovCertificate
    ) {
        val test = covCertificate.dgcEntry as Test
        val isOlder = test.sampleCollection?.isOlderThan(
            Test.ANTIGEN_TEST_EXPIRY_TIME_HOURS
        ) ?: false
        eventNotifier {
            if (isOlder) {
                eventNotifier {
                    onNegativeExpiredAntigenTest(
                        covCertificate,
                        test.sampleCollection
                    )
                }
            } else {
                onNegativeValidAntigenTest(
                    covCertificate,
                    test.sampleCollection
                )
            }
        }
    }
}
