/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing.data.validate

import kotlinx.serialization.Serializable

@Serializable
public data class TicketingValidationRequest(
    val kid: String,
    val dcc: String,
    val sig: String,
    val encKey: String,
    val encScheme: String = "RSAOAEPWithSHA256AESGCM",
    val sigAlg: String = "SHA256withECDSA",
)
