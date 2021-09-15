package de.rki.covpass.sdk.rules.local

import androidx.room.TypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import dgca.verifier.app.engine.UTC_ZONE_ID
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime

public class Converters {
    @TypeConverter
    public fun timestampToLocalDate(value: Long?): LocalDate = if (value != null) {
        val instant: Instant = Instant.ofEpochMilli(value)
        ZonedDateTime.ofInstant(instant, UTC_ZONE_ID)
    } else {
        ZonedDateTime.now(UTC_ZONE_ID)
    }.toLocalDate()

    @TypeConverter
    public fun localDateToTimestamp(localDate: LocalDate?): Long {
        return (
            localDate?.atStartOfDay(UTC_ZONE_ID)
                ?: ZonedDateTime.now(UTC_ZONE_ID)
            ).toInstant().toEpochMilli()
    }

    @TypeConverter
    public fun fromTimestamp(value: Long?): ZonedDateTime = if (value != null) {
        val instant: Instant = Instant.ofEpochMilli(value)
        ZonedDateTime.ofInstant(instant, UTC_ZONE_ID)
    } else {
        ZonedDateTime.now(UTC_ZONE_ID)
    }

    @TypeConverter
    public fun zonedDateTimeToTimestamp(zonedDateTime: ZonedDateTime?): Long {
        return (
            zonedDateTime?.withZoneSameInstant(UTC_ZONE_ID)
                ?: ZonedDateTime.now(UTC_ZONE_ID)
            ).toInstant().toEpochMilli()
    }

    @TypeConverter
    public fun fromString(value: String?): List<String> {
        val objectMap = ObjectMapper()
        return objectMap.readValue(value, Array<String>::class.java).toList()
    }

    @TypeConverter
    public fun fromList(list: List<String?>?): String {
        val objectMapper = ObjectMapper()
        return objectMapper.writeValueAsString(list ?: emptyList<String>())
    }
}
