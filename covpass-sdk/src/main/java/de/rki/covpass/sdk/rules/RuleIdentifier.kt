/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules

public data class RuleIdentifier(
    val identifier: String,
    val version: String,
    val country: String,
    val hash: String
)
