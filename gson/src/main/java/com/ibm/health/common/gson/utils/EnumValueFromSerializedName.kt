package com.ibm.health.common.gson.utils

import com.google.gson.annotations.SerializedName

/** Mixin for enums that adds a `value` attribute which returns the `@SerializedName` annotation's value. */
public interface EnumValueFromSerializedName {
    public val name: String

    public val value: String
        get() = this::class.java.getDeclaredField(name).getAnnotation(SerializedName::class.java).value
}
