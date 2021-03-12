package com.ibm.health.common.gson.adapter

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.lang.reflect.Type

public class ZonedDateTimeAdapter(
    private val serializeToString: Boolean = true,
    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
) : JsonSerializer<ZonedDateTime>, JsonDeserializer<ZonedDateTime> {

    override fun serialize(src: ZonedDateTime?, typeOfSrc: Type?, ctx: JsonSerializationContext?): JsonElement =
        src?.let {
            ctx?.serialize(
                if (serializeToString)
                    src.format(formatter)
                else
                    mapOf(
                        "year" to it.year,
                        "month" to it.month.value,
                        "day" to it.dayOfMonth,
                        "hour" to it.hour,
                        "minute" to it.minute,
                        "second" to it.second,
                        "nano" to it.nano,
                        "zone" to it.zone.id
                    )
            )
        } ?: JsonNull.INSTANCE

    override fun deserialize(json: JsonElement?, typeOfT: Type?, ctx: JsonDeserializationContext?): ZonedDateTime? =
        json?.let {
            if (it.isJsonObject) {
                ZonedDateTime.of(
                    json.asJsonObject.get("year").asInt,
                    json.asJsonObject.get("month").asInt,
                    json.asJsonObject.get("day").asInt,
                    json.asJsonObject.get("hour").asInt,
                    json.asJsonObject.get("minute").asInt,
                    json.asJsonObject.get("second").asInt,
                    json.asJsonObject.get("nano").asInt,
                    ZoneId.of(json.asJsonObject.get("zone").asString),
                )
            } else {
                ZonedDateTime.parse(it.asString, formatter)
            }
        }
}
