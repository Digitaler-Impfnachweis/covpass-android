@file:UseSerializers(LocalDateSerializer::class)
package com.ibm.health.vaccination.sdk.android.qr.decode.models

import kotlinx.serialization.UseSerializers
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
public data class Vaccination(
    val targetDisease: String = "",
    val vaccineCode: String = "",
    val product: String = "",
    val manufacturer: String = "",
    val series: String = "",
    val lotNumber: String = "",
    val occurence: LocalDate? = null,
    val location: String = "",
    val performer: String = "",
    val country: String = "",
    val nextDate: LocalDate? = null,
)
