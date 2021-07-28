/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

public data class EUValueSetValue(
    val display: String,
    val lang: String,
    val active: Boolean,
    val system: String,
    val version: String
)
