package com.ibm.health.vaccination.sdk.android.utils

import android.app.Application

internal fun Application.readTextAsset(path: String): String =
    assets.open(path).bufferedReader().use { it.readText() }
