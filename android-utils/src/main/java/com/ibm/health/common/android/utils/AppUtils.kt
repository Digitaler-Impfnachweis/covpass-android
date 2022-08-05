/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import com.ensody.reactivestate.DependencyAccessor

/** The version of the application. */
@OptIn(DependencyAccessor::class)
public val packageName: String by lazy {
    androidDeps.application.packageName
}

/** The version of the application. */
@OptIn(DependencyAccessor::class)
public val appVersion: String by lazy {
    androidDeps.application.appVersion
}

/** The version of the application. */
public val Application.appVersion: String get() =
    packageManager.getPackageInfo(packageName, 0).versionName

/** True, if the application is debuggable. */
@OptIn(DependencyAccessor::class)
public val isDebuggable: Boolean by lazy {
    androidDeps.application.isDebuggable
}

/** True, if the application is debuggable. */
public val Context.isDebuggable: Boolean get() =
    applicationInfo.flags.hasFlags(ApplicationInfo.FLAG_DEBUGGABLE)

/** True, if the given bit [flags] are set. */
public fun Int.hasFlags(flags: Int): Boolean =
    this and flags == flags
