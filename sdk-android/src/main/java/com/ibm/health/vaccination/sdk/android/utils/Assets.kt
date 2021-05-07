package com.ibm.health.vaccination.sdk.android.utils

import android.app.Application

internal fun Application.readTextAsset(path: String): String =
    assets.open("backend-ca.pem").bufferedReader().use { it.readText() }
