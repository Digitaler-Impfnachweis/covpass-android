package com.ibm.health.common.gson.adapter

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.ibm.health.common.gson.validateGsonField
import kotlin.collections.set

/**
 * Type Adapter for enums that is automatically added to the default common `Gson` object
 */
public object EnumTypeAdapterFactory : TypeAdapterFactory {
    override fun <T : Any?> create(gson: Gson?, type: TypeToken<T>?): TypeAdapter<T>? {
        val rawType = type?.rawType

        if (rawType?.isEnum != true) {
            return null
        }

        val enumKeyToConstant: MutableMap<String, T> = HashMap()
        for (constant in rawType.enumConstants) {
            val key = getKeyForConstant(rawType, constant as T)
            enumKeyToConstant[key] = constant as T
        }

        return object : TypeAdapter<T>() {
            override fun write(out: JsonWriter, value: T) {
                if (value == null) {
                    out.nullValue()
                } else {
                    out.value(getKeyForConstant(rawType, value))
                }
            }

            override fun read(reader: JsonReader): T? {
                return if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull()
                    null
                } else {
                    enumKeyToConstant[reader.nextString()]
                }
            }
        }
    }

    private fun <T> getKeyForConstant(rawType: Class<in T>, constant: T?): String {
        val field = rawType.fields.find { it.name == constant.toString() }
            ?: throw ObfuscatedEnumField(rawType, constant.toString())
        validateGsonField(field)
        return field.getAnnotation(SerializedName::class.java)?.value
            ?: constant.toString()
    }
}

public class ObfuscatedEnumField(public val cls: Class<*>, public val name: String) : IllegalArgumentException(
    "Missing annotation to prevent R8/ProGuard obfuscation: $cls::$name"
)
