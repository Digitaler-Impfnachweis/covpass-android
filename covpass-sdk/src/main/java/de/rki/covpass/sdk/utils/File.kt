/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

public fun String.sanitizeFileName(): String {
    var prefix = substringBeforeLast(".")
    if (prefix.endsWith(".tar")) {
        prefix = prefix.substringBeforeLast(".")
    }
    val extension = substring(prefix.length)
    val regex = Regex("[*\\\\/.?]+")
    val baseName = regex.replace(prefix, "-")
    return "$baseName$extension"
}
