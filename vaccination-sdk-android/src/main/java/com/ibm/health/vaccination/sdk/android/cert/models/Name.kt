/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.vaccination.sdk.android.cert.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Name(
    @SerialName("gn")
    val givenName: String? = null,
    @SerialName("fn")
    val familyName: String? = null,
    @SerialName("gnt")
    val givenNameTransliterated: String? = null,
    @SerialName("fnt")
    val familyNameTransliterated: String = ""
)
