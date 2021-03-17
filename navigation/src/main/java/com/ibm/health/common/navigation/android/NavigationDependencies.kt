package com.ibm.health.common.navigation.android

import android.app.Application

public lateinit var navigationDeps: NavigationDependencies

public abstract class NavigationDependencies {
    public abstract val application: Application

    public open val defaultScreenOrientation: Orientation = Orientation.SENSOR

    public open val animationConfig: NavigationAnimationConfig = DefaultNavigationAnimationConfig()
}
