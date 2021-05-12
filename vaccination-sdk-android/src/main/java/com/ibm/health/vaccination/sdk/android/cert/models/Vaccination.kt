@file:UseSerializers(LocalDateSerializer::class)

package com.ibm.health.vaccination.sdk.android.cert.models

import com.ibm.health.vaccination.sdk.android.utils.isOlderThanTwoWeeks
import com.ibm.health.vaccination.sdk.android.utils.serialization.LocalDateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDate

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
    @SerialName("dt")
    val occurrence: LocalDate? = null,
    @SerialName("co")
    val country: String = "",
    @SerialName("is")
    val certificateIssuer: String = "",
    @SerialName("ci")
    val id: String = ""
) {
    public val isComplete: Boolean
        get() = doseNumber == totalSerialDoses

    public val hasFullProtection: Boolean
        get() = isComplete && occurrence.isOlderThanTwoWeeks()
}
