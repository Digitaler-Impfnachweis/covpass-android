@file:UseSerializers(LocalDateSerializer::class, SexSerializer::class)

package com.ibm.health.vaccination.sdk.android.cert.models

import com.ibm.health.vaccination.sdk.android.utils.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDate

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

    public val isComplete: Boolean
        get() = vaccination.any { it.isComplete }

    public val hasFullProtection: Boolean
        get() = vaccination.any { it.hasFullProtection }

    public val currentSeries: String
        get() = vaccination.first().currentSeries

    public val completeSeries: String
        get() = vaccination.first().completeSeries

    public val validDate: LocalDate?
        get() = vaccination.first().occurrence?.plusDays(15)
}
