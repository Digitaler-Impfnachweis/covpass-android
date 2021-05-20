/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.navigation.android

import android.app.Application

/**
 * Global var for making the [NavigationDependencies] accessible.
 */
public lateinit var navigationDeps: NavigationDependencies

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
