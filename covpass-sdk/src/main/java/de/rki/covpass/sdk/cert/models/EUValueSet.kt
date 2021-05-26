/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import kotlinx.serialization.Serializable

@Serializable
internal data class EUValueSet(
    val valueSetId: String,
    val valueSetDate: String,
    val valueSetValues: Map<String, EUValueSetValue>
)
