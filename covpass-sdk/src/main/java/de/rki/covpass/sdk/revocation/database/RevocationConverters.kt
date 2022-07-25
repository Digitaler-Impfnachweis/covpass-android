package de.rki.covpass.sdk.revocation.database

import androidx.room.TypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.covpass.sdk.dependencies.defaultJson
import de.rki.covpass.sdk.revocation.RevocationIndexEntry
import de.rki.covpass.sdk.utils.decodeHex
import de.rki.covpass.sdk.utils.toHex
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

public class RevocationConverters {
    @TypeConverter
    public fun byteArrayToString(value: ByteArray?): String = value?.toHex() ?: ""

    @TypeConverter
    public fun stringToByteArray(value: String?): ByteArray = value?.decodeHex() ?: byteArrayOf()

    @TypeConverter
    public fun byteArrayListToString(list: List<ByteArray>?): String {
        val newList = list?.map {
            it.toHex()
        }
        val objectMapper = ObjectMapper()
        return objectMapper.writeValueAsString(newList ?: emptyList<String>())
    }

    @TypeConverter
    public fun stringToByteArrayList(value: String?): List<ByteArray> {
        val objectMap = ObjectMapper()
        val list = objectMap.readValue(value, Array<String>::class.java).toList()
        return list.map { it.decodeHex() }
    }

    @TypeConverter
    public fun stringToHashVariantMap(value: String): Map<Byte, Int> {
        return defaultJson.decodeFromString(value)
    }

    @TypeConverter
    public fun hashVariantMapToString(map: Map<Byte, Int>): String {
        return defaultJson.encodeToString(map)
    }

    @TypeConverter
    public fun stringToIndexMap(value: String): Map<Byte, RevocationIndexEntry> {
        return defaultJson.decodeFromString(value)
    }

    @TypeConverter
    public fun indexMapToString(map: Map<Byte, RevocationIndexEntry>): String {
        return defaultJson.encodeToString(map)
    }
}
