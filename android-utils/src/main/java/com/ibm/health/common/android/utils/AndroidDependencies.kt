package com.ibm.health.common.android.utils

import android.app.Activity
import android.app.Application

/** Global var for making the [AndroidDependencies] accessible. */
public lateinit var androidDeps: AndroidDependencies

/** Access to various dependencies for android-utils module. */
public abstract class AndroidDependencies {

    /** The android [Application]. */
    public abstract val application: Application

    /** The current [Activity]. */
    public open fun currentActivityOrNull(): Activity? = null

    /** The [ResourceProvider]. */
    public val resourceProvider: ResourceProvider by lazy {
        ResourceProvider(application)
    }
}
