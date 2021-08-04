/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import java.security.MessageDigest

internal fun String.sha512(): ByteArray =
    toByteArray().sha512()

internal fun ByteArray.sha512(): ByteArray =
    MessageDigest.getInstance("SHA-512").digest(this)
