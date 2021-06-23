/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import de.rki.covpass.sdk.cert.models.DGCEntry
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.Test
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.utils.isOlderThan
import java.lang.RuntimeException

public const val RULE_VR_DE_001: String = "VR_DE_001"
public const val RULE_VR_DE_002: String = "VR_DE_002"
public const val RULE_VR_DE_003: String = "VR_DE_003"
public const val RULE_VR_DE_004: String = "VR_DE_004"
public const val RULE_TR_DE_001: String = "TR_DE_001"
public const val RULE_TR_DE_002: String = "TR_DE_002"
public const val RULE_TR_DE_003: String = "TR_DE_003"
public const val RULE_TR_DE_004: String = "TR_DE_004"
public const val RULE_RR_DE_001: String = "RR_DE_001"
public const val RULE_RR_DE_002: String = "RR_DE_002"

/**
 * Asserts that the german validation rule set for the given [DGCEntry] is not violated.
 *
 * @throws ValidationRuleViolationException, if some of the validation rules is violated.
 *
 * TODO This will be replaced later on by the EU rule set.
 * TODO This will also be used later on in CovPass (currently only used in CovPassCheck).
 */
public fun assertGermanValidationRuleSet(dgcEntry: DGCEntry) {
    when (dgcEntry) {
        is Vaccination -> {
            assertVaccinationRules(dgcEntry)
        }
        is Test -> {
            assertTestRules(dgcEntry)
        }
        is Recovery -> {
            assertRecoveryRules(dgcEntry)
        }
    }
}

private fun assertVaccinationRules(vaccination: Vaccination) {
    assertRuleVrDe001(vaccination)
    assertRuleVrDe002(vaccination)
    assertRuleVrDe003(vaccination)
    assertRuleVrDe004(vaccination)
}

private fun assertTestRules(test: Test) {
    assertRuleTrDe001(test)
    assertRuleTrDe002(test)
    assertRuleTrDe003(test)
    assertRuleTrDe004(test)
}

private fun assertRecoveryRules(recovery: Recovery) {
    assertRuleRrDe001(recovery)
    assertRuleRrDe002(recovery)
}

private fun assertRuleVrDe001(vaccination: Vaccination) {
    assertValidationSuccess(vaccination.doseNumber == vaccination.totalSerialDoses, RULE_VR_DE_001)
}

private fun assertRuleVrDe002(vaccination: Vaccination) {
    assertValidationSuccess(
        vaccination.product in listOf(
            Vaccination.PRODUCT_COMIRNATY,
            Vaccination.PRODUCT_JANSSEN,
            Vaccination.PRODUCT_MODERNA,
            Vaccination.PRODUCT_VAXZEVRIA
        ),
        RULE_VR_DE_002
    )
}

private fun assertRuleVrDe003(vaccination: Vaccination) {
    assertValidationSuccess(vaccination.occurrence?.isOlderThan(days = 14) == true, RULE_VR_DE_003)
}

private fun assertRuleVrDe004(vaccination: Vaccination) {
    assertValidationSuccess(vaccination.occurrence?.isOlderThan(days = 365) == false, RULE_VR_DE_004)
}

private fun assertRuleTrDe001(test: Test) {
    assertValidationSuccess(test.testType in listOf(Test.PCR_TEST, Test.ANTIGEN_TEST), RULE_TR_DE_001)
}

private fun assertRuleTrDe002(test: Test) {
    assertValidationSuccess(
        test.testType != Test.ANTIGEN_TEST ||
            test.sampleCollection?.isOlderThan(Test.ANTIGEN_TEST_EXPIRY_TIME_HOURS) == false,
        RULE_TR_DE_002
    )
}

private fun assertRuleTrDe003(test: Test) {
    assertValidationSuccess(
        test.testType != Test.PCR_TEST ||
            test.sampleCollection?.isOlderThan(Test.PCR_TEST_EXPIRY_TIME_HOURS) == false,
        RULE_TR_DE_003
    )
}

private fun assertRuleTrDe004(test: Test) {
    assertValidationSuccess(test.testResult == Test.NEGATIVE_RESULT, RULE_TR_DE_004)
}

private fun assertRuleRrDe001(recovery: Recovery) {
    assertValidationSuccess(recovery.firstResult?.isOlderThan(27) == true, RULE_RR_DE_001)
}

private fun assertRuleRrDe002(recovery: Recovery) {
    assertValidationSuccess(recovery.firstResult?.isOlderThan(180) == false, RULE_RR_DE_002)
}

private fun assertValidationSuccess(success: Boolean, ruleIdentifier: String) {
    if (!success) throw ValidationRuleViolationException(ruleIdentifier)
}

/**
 * This exception is thrown when a validation rule is violated.
 *
 * @param ruleIdentifier For identifying the rule, e.g. RR_DE_002
 */
public class ValidationRuleViolationException(public val ruleIdentifier: String) :
    RuntimeException("Violation of validation rule $ruleIdentifier")
