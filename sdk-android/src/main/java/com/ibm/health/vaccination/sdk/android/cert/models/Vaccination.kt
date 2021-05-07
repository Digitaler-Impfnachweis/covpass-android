@file:UseSerializers(LocalDateSerializer::class)

package com.ibm.health.vaccination.sdk.android.cert.models

import com.ibm.health.common.vaccination.app.utils.isOlderThanTwoWeeks
import com.ibm.health.vaccination.sdk.android.utils.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDate

@Serializable
public data class ExtendedVaccination(
    val targetDisease: String = "",
    val vaccineCode: String = "",
    val product: String = "",
    val manufacturer: String = "",
    val series: String = "",
    val lotNumber: String = "",
    val occurrence: LocalDate? = null,
    val location: String = "",
    val performer: String = "",
    val country: String = "",
    val nextDate: LocalDate? = null,
) {
    public val isComplete: Boolean
        get() = getCurrentSeries(series) == getCompleteSeries(series)

    public val hasFullProtection: Boolean
        get() = isComplete && occurrence.isOlderThanTwoWeeks()

    public val currentSeries: String
        get() = getCurrentSeries(series)

    public val completeSeries: String
        get() = getCompleteSeries(series)
}

@Serializable
public data class Vaccination(
    val targetDisease: String = "",
    val vaccineCode: String = "",
    val product: String = "",
    val manufacturer: String = "",
    val series: String = "",
    val occurrence: LocalDate? = null,
    val country: String = "",
) {
    public val hasFullProtection: Boolean
        get() = isComplete && occurrence.isOlderThanTwoWeeks()

    private val isComplete: Boolean
        get() = getCurrentSeries(series) == getCompleteSeries(series)
}

private fun getCurrentSeries(series: String): String {
    val seriesValues = series.split("/")
    return seriesValues[0]
}

private fun getCompleteSeries(series: String): String {
    val seriesValues = series.split("/")
    return seriesValues[1]
}
