package de.rki.covpass.commonapp.utils

import de.rki.covpass.sdk.cert.models.DGCEntry
import de.rki.covpass.sdk.cert.models.Test
import de.rki.covpass.sdk.cert.models.Vaccination

/**
 * Helper class with returns the [CertificateType] based on the given [DGCEntry]
 */
public object CertificateHelper {

    public fun resolveCertificateType(dgcEntry: DGCEntry): CertificateType {
        return when (dgcEntry) {
            is Vaccination -> {
                when {
                    dgcEntry.hasFullProtection -> {
                        CertificateType.VACCINATION_FULL_PROTECTION
                    }
                    dgcEntry.isComplete -> {
                        CertificateType.VACCINATION_COMPLETE
                    }
                    else -> CertificateType.VACCINATION_INCOMPLETE
                }
            }
            is Test -> resolveTestType(dgcEntry)
            else -> CertificateType.RECOVERY
        }
    }

    private fun resolveTestType(test: Test): CertificateType {
        return when (test.testType) {
            Test.PCR_TEST -> {
                when (test.testResult) {
                    Test.POSITIVE_RESULT -> {
                        CertificateType.POSITIVE_PCR_TEST
                    }
                    Test.NEGATIVE_RESULT -> {
                        CertificateType.NEGATIVE_PCR_TEST
                    }
                    else -> throw IllegalStateException(
                        "Invalid testResult: ${test.testResult} for testType: ${test.testType}"
                    )
                }
            }
            Test.ANTIGEN_TEST -> {
                when (test.testResult) {
                    Test.POSITIVE_RESULT -> {
                        CertificateType.POSITIVE_ANTIGEN_TEST
                    }
                    Test.NEGATIVE_RESULT -> {
                        CertificateType.NEGATIVE_ANTIGEN_TEST
                    }
                    else -> throw IllegalStateException(
                        "Invalid testResult: ${test.testResult} for testType: ${test.testType}"
                    )
                }
            }
            else -> throw IllegalStateException("Invalid testType: ${test.testType}")
        }
    }
}
