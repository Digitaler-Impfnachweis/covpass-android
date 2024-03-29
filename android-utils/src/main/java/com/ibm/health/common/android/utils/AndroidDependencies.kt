/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.ensody.reactivestate.DependencyAccessor

/** Global var for making the [AndroidDependencies] accessible. */
@DependencyAccessor
public lateinit var androidDeps: AndroidDependencies

@OptIn(DependencyAccessor::class)
public val LifecycleOwner.androidDeps: AndroidDependencies get() = com.ibm.health.common.android.utils.androidDeps

@OptIn(DependencyAccessor::class)
public fun initAndroidDepsIfNeeded(application: Application) {
    if (!::androidDeps.isInitialized) {
        androidDeps = object : AndroidDependencies() {
            override val application: Application = application
        }
    }
}

/** Access to various dependencies for android-utils module. */
public abstract class AndroidDependencies {

    /** The android [Application]. */
    public abstract val application: Application

    /** The current [Activity]. */
    public open fun currentActivityOrNull(): Activity? = null

    /** The current [Activity] with fallback to [Application]. */
    public open fun currentActivityOrApplication(): Context = currentActivityOrNull() ?: application

    /** A context that uses the correct locale. @see [LocaleContextWrapper] */
    public open val localeRespectingContext: Context by lazy {
        LocaleContextWrapper.wrapContext(application)
    }

    /** The [ResourceProvider]. */
    public open val resourceProvider: ResourceProvider by lazy {
        ResourceProvider(localeRespectingContext)
    }
}
