@file:UseSerializers(LocalDateSerializer::class, SexSerializer::class)
package com.ibm.health.vaccination.sdk.android.qr.models

import com.ibm.health.vaccination.sdk.android.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDate
import com.ibm.health.common.vaccination.app.utils.getFormattedDate

/**
 * Data model for the vaccination certificate
 */
@Serializable
public data class VaccinationCertificate(
    val name: String = "",
    val birthDate: LocalDate? = null,
    val identifier: String = "",
    val sex: Sex? = null,
    val vaccination: List<ExtendedVaccination> = emptyList(),
    val issuer: String = "",
    val id: String = "",
    val validFrom: LocalDate? = null,
    val validUntil: LocalDate? = null,
    val version: String = "",
) {

    public val formattedBirthDate: String
        get() = birthDate?.getFormattedDate() ?: "-"

    public val isComplete: Boolean
        get() = vaccination.any { it.isComplete }

    public val currentSeries: String
        get() = vaccination.first().currentSeries

    public val completeSeries: String
        get() = vaccination.first().completeSeries
}
