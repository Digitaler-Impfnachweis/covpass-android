/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import android.app.Application
import assertk.assertThat
import assertk.assertions.isEqualTo
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.dependencies.SdkDependencies
import de.rki.covpass.sdk.dependencies.sdkDeps
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import java.security.cert.X509Certificate
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

internal class GermanValidationRuleSetTest {

    private val currentDateTime = LocalDateTime.of(2011, 5, 21, 13, 46)

    @Before
    fun setUp() {
        val fixedClock = Clock.fixed(
            currentDateTime.toInstant(ZoneOffset.UTC),
            ZoneId.of("UTC")
        )

        sdkDeps = object : SdkDependencies() {
            override val application: Application = mockk()
            override val clock: Clock = fixedClock
            override val backendCa: List<X509Certificate> = emptyList()
        }
    }

    @Test
    fun `Assert valid vaccination`() {
        val vaccination = Vaccination(
            product = Vaccination.PRODUCT_COMIRNATY,
            occurrence = currentDateTime.toLocalDate().minusDays(15)
        )
        assertGermanValidationRuleSet(vaccination)
    }

    @Test
    fun `Assert 365 days old vaccination`() {
        val vaccination = Vaccination(
            product = Vaccination.PRODUCT_COMIRNATY,
            occurrence = currentDateTime.toLocalDate().minusDays(365)
        )
        assertGermanValidationRuleSet(vaccination)
    }

    @Test
    fun `Assert violation of VR_DE_001`() {
        var ruleIdentifier: String? = null
        try {
            val vaccination = Vaccination(
                product = Vaccination.PRODUCT_COMIRNATY,
                doseNumber = 1,
                totalSerialDoses = 2,
                occurrence = currentDateTime.toLocalDate().minusDays(15)
            )
            assertGermanValidationRuleSet(vaccination)
        } catch (exception: ValidationRuleViolationException) {
            ruleIdentifier = exception.ruleIdentifier
        }
        assertThat(ruleIdentifier).isEqualTo(RULE_VR_DE_001)
    }

    @Test
    fun `Assert violation of VR_DE_002`() {
        var ruleIdentifier: String? = null
        try {
            val vaccination = Vaccination(
                product = "unknown",
                occurrence = currentDateTime.toLocalDate().minusDays(15)
            )
            assertGermanValidationRuleSet(vaccination)
        } catch (exception: ValidationRuleViolationException) {
            ruleIdentifier = exception.ruleIdentifier
        }
        assertThat(ruleIdentifier).isEqualTo(RULE_VR_DE_002)
    }

    @Test
    fun `Assert violation of VR_DE_003`() {
        var ruleIdentifier: String? = null
        try {
            val vaccination = Vaccination(
                product = Vaccination.PRODUCT_COMIRNATY,
                occurrence = currentDateTime.toLocalDate().minusDays(14)
            )
            assertGermanValidationRuleSet(vaccination)
        } catch (exception: ValidationRuleViolationException) {
            ruleIdentifier = exception.ruleIdentifier
        }
        assertThat(ruleIdentifier).isEqualTo(RULE_VR_DE_003)
    }

    @Test
    fun `Assert violation of VR_DE_004`() {
        var ruleIdentifier: String? = null
        try {
            val vaccination = Vaccination(
                product = Vaccination.PRODUCT_COMIRNATY,
                occurrence = currentDateTime.toLocalDate().minusDays(366)
            )
            assertGermanValidationRuleSet(vaccination)
        } catch (exception: ValidationRuleViolationException) {
            ruleIdentifier = exception.ruleIdentifier
        }
        assertThat(ruleIdentifier).isEqualTo(RULE_VR_DE_004)
    }

    @Test
    fun `Assert valid test`() {
        val test = de.rki.covpass.sdk.cert.models.Test(
            testType = de.rki.covpass.sdk.cert.models.Test.PCR_TEST,
            sampleCollection = ZonedDateTime.of(
                currentDateTime.minusDays(3), ZoneId.of("UTC")
            ),
            testResult = de.rki.covpass.sdk.cert.models.Test.NEGATIVE_RESULT
        )
        assertGermanValidationRuleSet(test)
    }

    @Test
    fun `Assert 48 hours old antigen test`() {
        val test = de.rki.covpass.sdk.cert.models.Test(
            testType = de.rki.covpass.sdk.cert.models.Test.ANTIGEN_TEST,
            sampleCollection = ZonedDateTime.of(
                currentDateTime.minusDays(2), ZoneId.of("UTC")
            ),
            testResult = de.rki.covpass.sdk.cert.models.Test.NEGATIVE_RESULT
        )
        assertGermanValidationRuleSet(test)
    }

    @Test
    fun `Assert violation of TR_DE_001`() {
        var ruleIdentifier: String? = null
        try {
            val test = de.rki.covpass.sdk.cert.models.Test(
                testType = "unknown",
                sampleCollection = ZonedDateTime.of(
                    currentDateTime.minusDays(3), ZoneId.of("UTC")
                ),
                testResult = de.rki.covpass.sdk.cert.models.Test.NEGATIVE_RESULT
            )
            assertGermanValidationRuleSet(test)
        } catch (exception: ValidationRuleViolationException) {
            ruleIdentifier = exception.ruleIdentifier
        }
        assertThat(ruleIdentifier).isEqualTo(RULE_TR_DE_001)
    }

    @Test
    fun `Assert violation of TR_DE_002`() {
        var ruleIdentifier: String? = null
        try {
            val test = de.rki.covpass.sdk.cert.models.Test(
                testType = de.rki.covpass.sdk.cert.models.Test.ANTIGEN_TEST,
                sampleCollection = ZonedDateTime.of(
                    currentDateTime.minusDays(2).minusMinutes(1), ZoneId.of("UTC")
                ),
                testResult = de.rki.covpass.sdk.cert.models.Test.NEGATIVE_RESULT
            )
            assertGermanValidationRuleSet(test)
        } catch (exception: ValidationRuleViolationException) {
            ruleIdentifier = exception.ruleIdentifier
        }
        assertThat(ruleIdentifier).isEqualTo(RULE_TR_DE_002)
    }

    @Test
    fun `Assert violation of TR_DE_003`() {
        var ruleIdentifier: String? = null
        try {
            val test = de.rki.covpass.sdk.cert.models.Test(
                testType = de.rki.covpass.sdk.cert.models.Test.PCR_TEST,
                sampleCollection = ZonedDateTime.of(
                    currentDateTime.minusDays(3).minusMinutes(1), ZoneId.of("UTC")
                ),
                testResult = de.rki.covpass.sdk.cert.models.Test.NEGATIVE_RESULT
            )
            assertGermanValidationRuleSet(test)
        } catch (exception: ValidationRuleViolationException) {
            ruleIdentifier = exception.ruleIdentifier
        }
        assertThat(ruleIdentifier).isEqualTo(RULE_TR_DE_003)
    }

    @Test
    fun `Assert violation of TR_DE_004`() {
        var ruleIdentifier: String? = null
        try {
            val test = de.rki.covpass.sdk.cert.models.Test(
                testType = de.rki.covpass.sdk.cert.models.Test.PCR_TEST,
                sampleCollection = ZonedDateTime.of(
                    currentDateTime.minusDays(3), ZoneId.of("UTC")
                ),
                testResult = de.rki.covpass.sdk.cert.models.Test.POSITIVE_RESULT
            )
            assertGermanValidationRuleSet(test)
        } catch (exception: ValidationRuleViolationException) {
            ruleIdentifier = exception.ruleIdentifier
        }
        assertThat(ruleIdentifier).isEqualTo(RULE_TR_DE_004)
    }

    @Test
    fun `Assert valid recovery`() {
        val recovery = Recovery(
            firstResult = currentDateTime.toLocalDate().minusDays(28)
        )
        assertGermanValidationRuleSet(recovery)
    }

    @Test
    fun `Assert 180 days old recovery`() {
        val recovery = Recovery(
            firstResult = currentDateTime.toLocalDate().minusDays(180)
        )
        assertGermanValidationRuleSet(recovery)
    }

    @Test
    fun `Assert violation of RR_DE_001`() {
        var ruleIdentifier: String? = null
        try {
            val recovery = Recovery(
                firstResult = currentDateTime.toLocalDate().minusDays(27)
            )
            assertGermanValidationRuleSet(recovery)
        } catch (exception: ValidationRuleViolationException) {
            ruleIdentifier = exception.ruleIdentifier
        }
        assertThat(ruleIdentifier).isEqualTo(RULE_RR_DE_001)
    }

    @Test
    fun `Assert violation of RR_DE_002`() {
        var ruleIdentifier: String? = null
        try {
            val recovery = Recovery(
                firstResult = currentDateTime.toLocalDate().minusDays(181)
            )
            assertGermanValidationRuleSet(recovery)
        } catch (exception: ValidationRuleViolationException) {
            ruleIdentifier = exception.ruleIdentifier
        }
        assertThat(ruleIdentifier).isEqualTo(RULE_RR_DE_002)
    }
}
