package de.rki.covpass.sdk.rules

public data class RuleIdentifier(
    val identifier: String,
    val version: String,
    val country: String,
    val hash: String
)
