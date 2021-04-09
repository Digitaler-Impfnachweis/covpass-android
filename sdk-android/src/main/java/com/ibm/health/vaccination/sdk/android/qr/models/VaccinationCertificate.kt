@file:UseSerializers(LocalDateSerializer::class, SexSerializer::class)
package com.ibm.health.vaccination.sdk.android.qr.models

import com.ibm.health.vaccination.sdk.android.serialization.LocalDateSerializer
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
    val vaccination: List<Vaccination> = emptyList(),
    val issuer: String = "",
    val id: String = "",
    val validFrom: LocalDate? = null,
    val validUntil: LocalDate? = null,
    val version: String = "",
    val secret: String = "",
)
