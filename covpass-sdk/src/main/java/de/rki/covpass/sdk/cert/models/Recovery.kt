/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

/**
 * Enum to mark the type of a [Recovery].
 */
public enum class RecoveryCertType : DGCEntryType {
    RECOVERY,
}

/**
 * Data model for the recoveries inside a Digital Green Certificate.
 */
@Serializable
public data class Recovery(
    @SerialName("tg")
    val targetDisease: String = "",
    @Contextual
    @SerialName("fr")
    val firstResult: LocalDate? = null,
    @Contextual
    @SerialName("df")
    val validFrom: LocalDate? = null,
    @Contextual
    @SerialName("du")
    val validUntil: LocalDate? = null,
    @SerialName("co")
    val country: String = "",
    @SerialName("is")
    val certificateIssuer: String = "",
    @SerialName("ci")
    override val id: String = "",
) : DGCEntry {

    override val type: DGCEntryType
        get() = RecoveryCertType.RECOVERY
}
