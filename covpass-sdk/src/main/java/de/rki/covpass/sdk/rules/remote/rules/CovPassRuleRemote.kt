/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.remote.rules

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.time.ZonedDateTime

@Serializable
public data class CovPassRuleRemote(
    @SerialName("Identifier")
    val identifier: String,
    @SerialName("Type")
    val type: String,
    @SerialName("Version")
    val version: String,
    @SerialName("SchemaVersion")
    val schemaVersion: String,
    @SerialName("Engine")
    val engine: String,
    @SerialName("EngineVersion")
    val engineVersion: String,
    @SerialName("CertificateType")
    val certificateType: String,
    @SerialName("Description")
    val descriptions: List<CovPassDescriptionRemote>,
    @Contextual
    @SerialName("ValidFrom")
    val validFrom: ZonedDateTime,
    @Contextual
    @SerialName("ValidTo")
    val validTo: ZonedDateTime,
    @SerialName("AffectedFields")
    val affectedString: List<String>,
    @SerialName("Logic")
    val logic: JsonElement,
    @SerialName("Country")
    val countryCode: String,
    @SerialName("Region")
    val region: String? = null,
)
