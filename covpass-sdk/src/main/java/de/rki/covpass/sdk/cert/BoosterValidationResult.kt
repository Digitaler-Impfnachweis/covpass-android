/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import de.rki.covpass.sdk.rules.booster.BoosterRule

public enum class BoosterResult {
    PASSED, FAIL, OPEN
}

public data class BoosterValidationResult(
    val rule: BoosterRule,
    val result: BoosterResult,
    val validationErrors: List<Throwable>?,
)
