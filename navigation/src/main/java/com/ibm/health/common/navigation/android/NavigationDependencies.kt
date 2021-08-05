/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.navigation.android

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import com.ensody.reactivestate.DependencyAccessor

/**
 * Global var for making the [NavigationDependencies] accessible.
 */
@DependencyAccessor
public lateinit var navigationDeps: NavigationDependencies

@OptIn(DependencyAccessor::class)
public val LifecycleOwner.navigationDeps: NavigationDependencies
    get() = com.ibm.health.common.navigation.android.navigationDeps

/**
 * Access to various dependencies for navigation module.
 */
public abstract class NavigationDependencies {

    /**
     * The android [Application].
     */
    public abstract val application: Application

    /**
     * The default screen [Orientation].
     */
    public open val defaultScreenOrientation: Orientation = Orientation.SENSOR

    /**
     * The [NavigationAnimationConfig].
     */
    public open val animationConfig: NavigationAnimationConfig = DefaultNavigationAnimationConfig()
}
