/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import de.rki.covpass.sdk.utils.isOlderThan
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

/**
 * Enum to mark the type of a [Vaccination].
 */
public enum class VaccinationCertType : DGCEntryType {
    VACCINATION_FULL_PROTECTION,
    VACCINATION_COMPLETE,
    VACCINATION_INCOMPLETE,
}

/**
 * Data model for the vaccinations inside a Digital Green Certificate.
 */
@Serializable
public data class Vaccination(
    @SerialName("tg")
    val targetDisease: String = "",
    @SerialName("vp")
    val vaccineCode: String = "",
    @SerialName("mp")
    val product: String = "",
    @SerialName("ma")
    val manufacturer: String = "",
    @SerialName("dn")
    val doseNumber: Int = 0,
    @SerialName("sd")
    val totalSerialDoses: Int = 0,
    @Contextual
    @SerialName("dt")
    val occurrence: LocalDate? = null,
    @SerialName("co")
    val country: String = "",
    @SerialName("is")
    val certificateIssuer: String = "",
    @SerialName("ci")
    override val id: String = ""
) : DGCEntry {
    public val isComplete: Boolean
        get() = doseNumber == totalSerialDoses

    public val isCompleteSingleDose: Boolean
        get() = doseNumber == 1 && totalSerialDoses == 1

    public val hasFullProtection: Boolean
        // Full protection is reached on day 15 after the complete vaccination
        get() = isComplete && occurrence?.isOlderThan(days = 14) == true

    public override val type: VaccinationCertType
        get() = when {
            hasFullProtection -> { VaccinationCertType.VACCINATION_FULL_PROTECTION }
            isComplete -> { VaccinationCertType.VACCINATION_COMPLETE }
            else -> VaccinationCertType.VACCINATION_INCOMPLETE
        }
}
