/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data model which contains the representation of a name according to the EU data model.
 */
@Serializable
public data class Name(
    @SerialName("gn")
    val givenName: String? = null,
    @SerialName("fn")
    val familyName: String? = null,
    @SerialName("gnt")
    val givenNameTransliterated: String? = null,
    @SerialName("fnt")
    val familyNameTransliterated: String = "",
) : java.io.Serializable {

    val trimmedName: Name
        get() = Name(
            givenName?.trim(),
            familyName?.trim(),
            givenNameTransliterated?.trim(),
            familyNameTransliterated.trim(),
        )
}
