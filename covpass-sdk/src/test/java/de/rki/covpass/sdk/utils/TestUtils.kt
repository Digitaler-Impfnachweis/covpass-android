/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import java.io.File

internal fun Any.readResource(path: String): String =
    String(javaClass.classLoader!!.getResourceAsStream(path).readBytes()).replace("\r\n", "\n")

internal fun readTextAssetFromTest(path: String): String =
    File("src/main/assets/$path").readText()
