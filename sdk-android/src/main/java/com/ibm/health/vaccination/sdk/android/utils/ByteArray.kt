package com.ibm.health.vaccination.sdk.android.utils

/** Converts this ByteArray to its hexadecimal representation. */
internal fun ByteArray.toHex(separator: String = " "): String =
    joinToString(separator) { it.toUByte().toString(16).padStart(2, '0') }
