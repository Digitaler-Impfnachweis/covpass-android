package com.ibm.health.common.android.utils

import android.app.Application
import android.content.pm.ApplicationInfo

/** The version of the application. */
public val appVersion: String by lazy {
    androidDeps.application.appVersion
}

/** The version of the application. */
public val Application.appVersion: String get() =
    packageManager.getPackageInfo(packageName, 0).versionName

/** True, if the application is debuggable. */
public val isDebuggable: Boolean by lazy {
    androidDeps.application.isDebuggable
}

/** True, if the application is debuggable. */
public val Application.isDebuggable: Boolean get() =
    applicationInfo.flags.hasFlags(ApplicationInfo.FLAG_DEBUGGABLE)

/** True, if the given bit [flags] are set. */
public fun Int.hasFlags(flags: Int): Boolean =
    this and flags == flags
