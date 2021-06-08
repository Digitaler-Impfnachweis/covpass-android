/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import android.app.Activity
import android.app.Application
import android.content.Context

/** Global var for making the [AndroidDependencies] accessible. */
public lateinit var androidDeps: AndroidDependencies

/** Access to various dependencies for android-utils module. */
public abstract class AndroidDependencies {

    /** The android [Application]. */
    public abstract val application: Application

    /** The current [Activity]. */
    public open fun currentActivityOrNull(): Activity? = null

    /** The current [Activity] with fallback to [Application]. */
    public open fun currentActivityOrApplication(): Context = currentActivityOrNull() ?: application

    /** The [ResourceProvider]. */
    public val resourceProvider: ResourceProvider by lazy {
        ResourceProvider(application)
    }
}
