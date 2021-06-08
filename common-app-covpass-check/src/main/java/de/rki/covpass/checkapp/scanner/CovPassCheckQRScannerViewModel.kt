/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.scanner

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import de.rki.covpass.logging.Lumber
import de.rki.covpass.sdk.cert.BadCoseSignatureException
import de.rki.covpass.sdk.cert.ExpiredCwtException
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.Test
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.utils.isOlderThan
import de.rki.covpass.sdk.utils.isValid
import kotlinx.coroutines.CoroutineScope
import java.time.ZonedDateTime

/**
 * Interface to communicate events from [CovPassCheckQRScannerViewModel] to [CovPassCheckQRScannerFragment].
 */
internal interface CovPassCheckQRScannerEvents : BaseEvents {
    // Vaccination
    fun onFullVaccination(certificate: CovCertificate)
    fun onPartialVaccination()

    // Test - PCR
    fun onPositivePcrTest(certificate: CovCertificate, sampleCollection: ZonedDateTime?)
    fun onNegativeValidPcrTest(certificate: CovCertificate, sampleCollection: ZonedDateTime?)
    fun onNegativeExpiredPcrTest(certificate: CovCertificate, sampleCollection: ZonedDateTime?)

    // Test - Antigen
    fun onPositiveAntigenTest(sampleCollection: ZonedDateTime?)
    fun onNegativeValidAntigenTest(certificate: CovCertificate, sampleCollection: ZonedDateTime?)
    fun onNegativeExpiredAntigenTest(certificate: CovCertificate, sampleCollection: ZonedDateTime?)

    // Recovery
    fun onValidRecoveryCert(certificate: CovCertificate)
    fun onExpiredRecoveryCert()

    fun onValidationFailure()
}

/**
 * ViewModel holding the business logic for decoding and validating a [CovCertificate].
 */
internal class CovPassCheckQRScannerViewModel(scope: CoroutineScope) : BaseState<CovPassCheckQRScannerEvents>(scope) {

    fun onQrContentReceived(qrContent: String) {
        launch {
            try {
                val covCertificate = sdkDeps.qrCoder.decodeCovCert(qrContent)
                when (val dgcEntry = covCertificate.dgcEntry) {
                    is Vaccination -> {
                        if (dgcEntry.hasFullProtection) {
                            eventNotifier { onFullVaccination(covCertificate) }
                        } else {
                            eventNotifier { onPartialVaccination() }
                        }
                    }
                    is Test -> {
                        when (dgcEntry.testType) {
                            Test.PCR_TEST -> handlePcrTest(covCertificate, dgcEntry)
                            Test.ANTIGEN_TEST -> handleAntigenTest(covCertificate, dgcEntry)
                        }
                    }
                    is Recovery -> {
                        if (isValid(dgcEntry.validFrom, dgcEntry.validUntil)) {
                            eventNotifier { onValidRecoveryCert(covCertificate) }
                        } else {
                            eventNotifier { onExpiredRecoveryCert() }
                        }
                    }
                }
            } catch (exception: Exception) {
                when (exception) {
                    is BadCoseSignatureException, is ExpiredCwtException -> {
                        Lumber.e(exception)
                        eventNotifier { onValidationFailure() }
                    }
                    else -> {
                        throw exception
                    }
                }
            }
        }
    }

    private fun handlePcrTest(
        covCertificate: CovCertificate,
        test: Test
    ) {
        when (test.testResult) {
            Test.NEGATIVE_RESULT ->
                handleNegativePcrResult(covCertificate, test)
            Test.POSITIVE_RESULT -> {
                eventNotifier {
                    onPositivePcrTest(
                        covCertificate,
                        test.sampleCollection
                    )
                }
            }
        }
    }

    private fun handleAntigenTest(
        covCertificate: CovCertificate,
        test: Test
    ) {
        when (test.testResult) {
            Test.NEGATIVE_RESULT ->
                handleNegativeAntigenResult(covCertificate, test)
            Test.POSITIVE_RESULT -> {
                eventNotifier {
                    onPositiveAntigenTest(
                        test.sampleCollection
                    )
                }
            }
        }
    }

    private fun handleNegativePcrResult(
        covCertificate: CovCertificate,
        test: Test
    ) {
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
        covCertificate: CovCertificate,
        test: Test
    ) {
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
