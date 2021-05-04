@file:UseSerializers(LocalDateSerializer::class)

package com.ibm.health.vaccination.sdk.android.cert.models

import com.ibm.health.vaccination.sdk.android.utils.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDate

/**
 * Data model for the validation certificate.
 */
@Serializable
public data class ValidationCertificate(
    val name: String = "",
    val birthDate: LocalDate? = null,
    val vaccination: List<Vaccination> = emptyList(),
    val issuer: String = "",
    val id: String = "",
    val validUntil: LocalDate? = null,
) {
    public val isComplete: Boolean
        get() = vaccination.any { it.isComplete }
}
