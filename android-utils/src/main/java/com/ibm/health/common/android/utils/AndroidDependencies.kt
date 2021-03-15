package com.ibm.health.common.android.utils

import android.app.Activity
import android.app.Application

public lateinit var androidDeps: AndroidDependencies

public abstract class AndroidDependencies {
    public abstract val application: Application

    public open fun currentActivityOrNull(): Activity? = null

    public val resourceProvider: ResourceProvider by lazy {
        ResourceProvider(application)
    }
}
