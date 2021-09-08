/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

/**
 * Enum to mark the type of a [TestCert].
 */
public enum class TestCertType : DGCEntryType {
    POSITIVE_PCR_TEST,
    NEGATIVE_PCR_TEST,
    POSITIVE_ANTIGEN_TEST,
    NEGATIVE_ANTIGEN_TEST,
}

/**
 * Data model for the tests inside a Digital Green Certificate.
 */
@Serializable
public data class TestCert(
    @SerialName("tg")
    val targetDisease: String = "",
    @SerialName("tt")
    val testType: String = "",
    @SerialName("nm")
    val testName: String? = "",
    @SerialName("ma")
    val manufacturer: String? = "",
    @Contextual
    @SerialName("sc")
    val sampleCollection: ZonedDateTime? = null,
    @SerialName("tr")
    val testResult: String = "",
    @SerialName("tc")
    val testingCenter: String = "",
    @SerialName("co")
    val country: String = "",
    @SerialName("is")
    val certificateIssuer: String = "",
    @SerialName("ci")
    override val id: String = ""
) : DGCEntry {

    public override val type: TestCertType
        get() = when (testType) {
            PCR_TEST -> {
                when (testResult) {
                    POSITIVE_RESULT -> { TestCertType.POSITIVE_PCR_TEST }
                    NEGATIVE_RESULT -> { TestCertType.NEGATIVE_PCR_TEST }
                    // Handle invalid testResult as positive test
                    else -> { TestCertType.POSITIVE_PCR_TEST }
                }
            }
            ANTIGEN_TEST -> {
                when (testResult) {
                    POSITIVE_RESULT -> { TestCertType.POSITIVE_ANTIGEN_TEST }
                    NEGATIVE_RESULT -> { TestCertType.NEGATIVE_ANTIGEN_TEST }
                    // Handle invalid testResult as positive test
                    else -> { TestCertType.POSITIVE_ANTIGEN_TEST }
                }
            }
            // Handle invalid testType as positive pcr test
            else -> { TestCertType.POSITIVE_PCR_TEST }
        }

    public val isPositive: Boolean
        get() = testResult == POSITIVE_RESULT

    public companion object {
        public const val PCR_TEST: String = "LP6464-4"
        public const val ANTIGEN_TEST: String = "LP217198-3"
        public const val POSITIVE_RESULT: String = "260373001"
        public const val NEGATIVE_RESULT: String = "260415000"
        public const val PCR_TEST_EXPIRY_TIME_HOURS: Long = 72
        public const val ANTIGEN_TEST_EXPIRY_TIME_HOURS: Long = 48
    }
}
