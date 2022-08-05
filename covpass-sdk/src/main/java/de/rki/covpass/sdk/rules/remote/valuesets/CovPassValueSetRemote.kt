/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.remote.valuesets

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.time.LocalDate

@Serializable
public data class CovPassValueSetRemote(
    val valueSetId: String,
    @Contextual
    val valueSetDate: LocalDate,
    val valueSetValues: JsonElement,
)
