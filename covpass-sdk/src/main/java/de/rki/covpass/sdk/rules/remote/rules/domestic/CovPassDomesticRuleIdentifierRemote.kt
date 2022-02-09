/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.remote.rules.domestic

import kotlinx.serialization.Serializable

@Serializable
public data class CovPassDomesticRuleIdentifierRemote(
    val identifier: String,
    val version: String,
    val hash: String
)
