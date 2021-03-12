package com.ibm.health.common.gson.adapter

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.threeten.bp.DayOfWeek
import java.lang.reflect.Type

public class DayOfWeekAdapter : JsonSerializer<DayOfWeek>, JsonDeserializer<DayOfWeek> {

    override fun serialize(src: DayOfWeek?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement =
        src?.let { context?.serialize(it.value) } ?: JsonNull.INSTANCE

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): DayOfWeek? =
        json?.let { if (it.isJsonPrimitive) DayOfWeek.of(it.asInt) else null }
}
