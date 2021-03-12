package com.ibm.health.common.gson

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import com.google.gson.annotations.SerializedName
import com.ibm.health.common.annotations.KeepFields
import org.junit.Test

private data class UnsafeData(
    @SerializedName("first_name")
    val firstName: String,
    val lastName: String,
)

private data class AnnotatedData(
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
)

private data class KeepFieldsData(
    val firstName: String,
    val lastName: String,
) : KeepFields

private enum class UnsafeEnum {
    @SerializedName("ValueX")
    VALUE_X,
    VALUE_Y,
}

private enum class AnnotatedEnum {
    @SerializedName("ValueX")
    VALUE_X,

    @SerializedName("ValueY")
    VALUE_Y,
}

private data class SafeDataWithUnsafeEnum(val unsafeEnum: UnsafeEnum) : KeepFields

private enum class KeepFieldsEnum : KeepFields { VALUE_X, VALUE_Y }

internal class GsonValidationTest {
    @Test
    fun `class validation`() {
        val annotated = AnnotatedData("Anna", "Banana")
        val annotatedJson = gson.toJson(annotated)
        assertThat(annotatedJson).isEqualTo("""{"first_name":"Anna","last_name":"Banana"}""")
        assertThat(gson.fromJson(annotatedJson, AnnotatedData::class.java)).isEqualTo(annotated)

        val keepFields = KeepFieldsData("Anna", "Banana")
        val keepFieldsJson = gson.toJson(keepFields)
        assertThat(keepFieldsJson).isEqualTo("""{"firstName":"Anna","lastName":"Banana"}""")
        assertThat(gson.fromJson(keepFieldsJson, KeepFieldsData::class.java)).isEqualTo(keepFields)

        // Although firstName is annotated, the whole validation fails because lastName isn't annotated
        // This way toJson fails early because fromJson wouldn't be able to handle all values correctly.
        assertThat { gson.toJson(UnsafeData("Anna", "Banana")) }
            .isFailure().isInstanceOf(ObfuscatedFieldName::class)
        assertThat { gson.fromJson(annotatedJson, UnsafeData::class.java) }
            .isFailure().isInstanceOf(ObfuscatedFieldName::class)
    }

    @Test
    fun `enum validation`() {
        val annotated = AnnotatedEnum.VALUE_X
        val annotatedJson = gson.toJson(annotated)
        assertThat(annotatedJson).isEqualTo("\"ValueX\"")
        assertThat(gson.fromJson(annotatedJson, AnnotatedEnum::class.java)).isEqualTo(annotated)

        val keepFields = KeepFieldsEnum.VALUE_X
        val keepFieldsJson = gson.toJson(keepFields)
        assertThat(keepFieldsJson).isEqualTo("\"VALUE_X\"")
        assertThat(gson.fromJson(keepFieldsJson, KeepFieldsEnum::class.java)).isEqualTo(keepFields)

        // Although VALUE_X is annotated, the whole validation fails because VALUE_Y isn't annotated.
        // This way toJson fails early because fromJson wouldn't be able to handle all values correctly.
        assertThat { gson.toJson(UnsafeEnum.VALUE_X) }
            .isFailure().isInstanceOf(ObfuscatedFieldName::class)
        assertThat { gson.toJson(SafeDataWithUnsafeEnum(UnsafeEnum.VALUE_X)) }
            .isFailure().isInstanceOf(ObfuscatedFieldName::class)
        assertThat { gson.fromJson(annotatedJson, UnsafeEnum::class.java) }
            .isFailure().isInstanceOf(ObfuscatedFieldName::class)
    }

    @Test
    fun `class field validation`() {
        // UnsafeData.firstName is annotated and doesn't fail
        validateGsonField(UnsafeData::class.java.getDeclaredField("firstName"))

        // UnsafeData.lastName lacks an annotation and fails
        assertThat { validateGsonField(UnsafeData::class.java.getDeclaredField("lastName")) }
            .isFailure().isInstanceOf(ObfuscatedFieldName::class)

        // KeepFieldsData isn't annotated, but still safe because it implements KeepFields
        validateGsonField(KeepFieldsData::class.java.getDeclaredField("firstName"))
        validateGsonField(KeepFieldsData::class.java.getDeclaredField("lastName"))
    }

    @Test
    fun `enum field validation`() {
        // UnsafeEnum.VALUE_X is annotated and doesn't fail
        validateGsonField(UnsafeEnum::class.java.getDeclaredField("VALUE_X"))

        // UnsafeEnum.VALUE_Y lacks an annotation and fails
        assertThat { validateGsonField(UnsafeEnum::class.java.getDeclaredField("VALUE_Y")) }
            .isFailure().isInstanceOf(ObfuscatedFieldName::class)

        // KeepFieldsEnum isn't annotated, but still safe because it implements KeepFields
        validateGsonField(KeepFieldsEnum::class.java.getDeclaredField("VALUE_X"))
        validateGsonField(KeepFieldsEnum::class.java.getDeclaredField("VALUE_Y"))
    }
}
