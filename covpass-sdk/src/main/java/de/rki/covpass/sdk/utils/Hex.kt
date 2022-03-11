/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

public fun ByteArray.toHex(): String =
    joinToString("") { "%02x".format(it) }
