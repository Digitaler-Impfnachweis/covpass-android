package com.ibm.health.common.gson.adapter

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.lang.reflect.Type

public class LocalDateAdapter(
    private val serializeToString: Boolean = true,
    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE
) : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

    override fun serialize(src: LocalDate?, typeOfSrc: Type?, ctx: JsonSerializationContext?): JsonElement =
        src?.let {
            ctx?.serialize(
                if (serializeToString)
                    src.format(formatter)
                else
                    mapOf(
                        "year" to it.year,
                        "dayOfYear" to it.dayOfYear
                    )
            )
        } ?: JsonNull.INSTANCE

    override fun deserialize(json: JsonElement?, typeOfT: Type?, ctx: JsonDeserializationContext?): LocalDate? =
        json?.let {
            if (it.isJsonObject) {
                LocalDate.ofYearDay(
                    json.asJsonObject.get("year").asInt,
                    json.asJsonObject.get("dayOfYear").asInt
                )
            } else {
                LocalDate.parse(it.asString, formatter)
            }
        }
}
