package com.ibm.health.common.gson.adapter

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import java.lang.reflect.Type

public class LocalTimeAdapter(
    private val serializeToString: Boolean = true,
    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_TIME
) : JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {

    override fun serialize(src: LocalTime?, typeOfSrc: Type?, ctx: JsonSerializationContext?): JsonElement =
        src?.let {
            ctx?.serialize(
                if (serializeToString)
                    src.format(formatter)
                else
                    mapOf(
                        "hour" to it.hour,
                        "minute" to it.minute,
                        "second" to it.second,
                        "nano" to it.nano
                    )
            )
        } ?: JsonNull.INSTANCE

    override fun deserialize(json: JsonElement?, typeOfT: Type?, ctx: JsonDeserializationContext?): LocalTime? =
        json?.let {
            if (it.isJsonObject) {
                LocalTime.of(
                    json.asJsonObject.get("hour").asInt,
                    json.asJsonObject.get("minute").asInt,
                    json.asJsonObject.get("second").asInt,
                    json.asJsonObject.get("nano").asInt,
                )
            } else {
                LocalTime.parse(it.asString, formatter)
            }
        }
}
