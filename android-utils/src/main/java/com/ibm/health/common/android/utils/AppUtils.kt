package com.ibm.health.common.android.utils

import android.app.Application
import android.content.pm.ApplicationInfo

public val appVersion: String by lazy {
    androidDeps.application.appVersion
}

public val Application.appVersion: String get() =
    packageManager.getPackageInfo(packageName, 0).versionName

public val isDebuggable: Boolean by lazy {
    androidDeps.application.isDebuggable
}

public val Application.isDebuggable: Boolean get() =
    applicationInfo.flags.hasFlags(ApplicationInfo.FLAG_DEBUGGABLE)

public fun Int.hasFlags(flags: Int): Boolean =
    this and flags == flags
