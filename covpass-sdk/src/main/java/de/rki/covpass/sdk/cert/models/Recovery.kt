/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

@file:UseSerializers(LocalDateSerializer::class)

package de.rki.covpass.sdk.cert.models

import de.rki.covpass.sdk.utils.serialization.LocalDateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDate

/**
 * Data model for the recoveries inside a Digital Green Certificate.
 */
@Serializable
public data class Recovery(
    @SerialName("tg")
    val targetDisease: String = "",
    @SerialName("fr")
    val firstResult: LocalDate? = null,
    @SerialName("df")
    val validFrom: LocalDate? = null,
    @SerialName("du")
    val validUntil: LocalDate? = null,
    @SerialName("co")
    val country: String = "",
    @SerialName("is")
    val certificateIssuer: String = "",
    @SerialName("ci")
    val id: String = ""
)
