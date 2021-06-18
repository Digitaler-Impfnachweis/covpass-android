/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import android.app.Application

/**
 * @return The content of the asset file at given [path] as string.
 */
internal fun Application.readTextAsset(path: String): String =
    assets.open(path).bufferedReader().use { it.readText().replace("\r\n", "\n") }
