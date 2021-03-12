package com.ibm.health.common.gson

import com.google.gson.FieldNamingPolicy
import com.google.gson.FieldNamingStrategy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.ibm.health.common.annotations.KeepFields
import com.ibm.health.common.gson.adapter.DayOfWeekAdapter
import com.ibm.health.common.gson.adapter.EnumTypeAdapterFactory
import com.ibm.health.common.gson.adapter.LocalDateAdapter
import com.ibm.health.common.gson.adapter.LocalTimeAdapter
import com.ibm.health.common.gson.adapter.ZonedDateTimeAdapter
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZonedDateTime
import java.lang.reflect.Field

/**
 * A [Gson] configuration with sensible defaults and R8/ProGuard-specific validation.
 *
 * By default, any object without explicit `@SerializedName` annotations will cause a [ObfuscatedFieldName] exception
 * during (de-)serialization because such classes would usually get obfuscated.
 *
 * However, if you have a common base interface for your JSON classes you could add respective R8 keep rules and
 * use [registerGsonValidator] to add a [GsonValidator] pass-through rule for your specific types.
 * For such classes, no exception is thrown even if your fields don't have any `@SerializedName` annotations.
 *
 * To simplify this even further, we provide such a default interface to prevent field obfuscation: [KeepFields]
 */
public val gson: Gson by lazy { buildGson() }

/** Our default [gson] serializer uses this builder. */
public fun GsonBuilder.defaultGsonBuilder(serializeNulls: Boolean = true) {
    // This is required in order to detect potential problems with R8 obfuscation. Only explicit annotations
    // are guaranteed to work correctly even if the field names get mangled.
    setFieldNamingStrategy(RequireExplicitFieldNameAnnotation(FieldNamingPolicy.IDENTITY))
    registerTypeAdapterFactory(EnumTypeAdapterFactory)
    registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
    registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
    registerTypeAdapter(ZonedDateTime::class.java, ZonedDateTimeAdapter())
    registerTypeAdapter(DayOfWeek::class.java, DayOfWeekAdapter())
    disableHtmlEscaping()
    if (serializeNulls) {
        serializeNulls()
    }
}

/** Creates a custom [Gson] instance based no [defaultGsonBuilder] and your additional builder. */
public fun buildGson(serializeNulls: Boolean = true, builder: GsonBuilder.() -> Unit = {}): Gson =
    GsonBuilder().apply {
        defaultGsonBuilder(serializeNulls)
        builder()
    }.create()

/** A validator used with [registerGsonValidator]. Return `true` to mark the type as having valid field names. */
public typealias GsonValidator = (f: Field) -> Boolean

/**
 * Registers a [GsonValidator] to be used with the [defaultGsonBuilder] and [gson] instance.
 *
 * Usually you should only use this in the module/repo that defines the affected type and the R8 consumer rules.
 */
public fun registerGsonValidator(validator: GsonValidator) {
    gsonValidators.add(validator)
}

/** Validates a [Field] for use with [Gson] and R8/ProGuard. Throws [ObfuscatedFieldName] if validation fails. */
public fun validateGsonField(field: Field) {
    // Explicit @SerializedName annotations are safe for runtime reflection and thus for R8/ProGuard
    if (field.getAnnotation(SerializedName::class.java) != null) {
        return
    }
    // Check other types of annotations
    for (validator in gsonValidators) {
        if (validator(field)) {
            return
        }
    }
    throw ObfuscatedFieldName(field)
}

private val gsonValidators: MutableList<GsonValidator> = mutableListOf(
    { KeepFields::class.java.isAssignableFrom(it.declaringClass) },
)

private class RequireExplicitFieldNameAnnotation(private val policy: FieldNamingPolicy) : FieldNamingStrategy {
    override fun translateName(f: Field): String {
        validateGsonField(f)
        return policy.translateName(f)
    }
}

/**
 * This exception indicates an incompatibility with R8/ProGuard.
 *
 * You're missing either [KeepFields] or `@[SerializedName]` or some other annotation - in combination with
 * [registerGsonValidator].
 */
public class ObfuscatedFieldName(public val f: Field) : IllegalArgumentException(
    "Missing annotation to prevent R8/ProGuard obfuscation: $f"
)
