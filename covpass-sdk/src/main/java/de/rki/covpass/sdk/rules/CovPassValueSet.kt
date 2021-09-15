/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules

import java.time.LocalDate

public data class CovPassValueSet(
    val valueSetId: String,
    val valueSetDate: LocalDate,
    val valueSetValues: String,
    val hash: String,
)
