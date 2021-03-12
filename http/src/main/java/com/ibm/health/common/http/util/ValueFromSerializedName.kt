package com.ibm.health.common.http.util

import com.google.gson.annotations.SerializedName

/** Mixin for enums that adds a `value` attribute which returns the `@SerializedName` annotation's value. */
public interface ValueFromSerializedName {
    public val name: String

    public val value: String
        get() = this::class.java.getDeclaredField(name).getAnnotation(SerializedName::class.java).value
}
