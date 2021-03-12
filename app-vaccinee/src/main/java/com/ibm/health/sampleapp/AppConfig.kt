package com.ibm.health.sampleapp

import android.app.Application

/** The main app configuration (and DI). */
interface AppConfig {
    val application: Application
}

abstract class BaseDefaultAppConfig(override val application: Application) : AppConfig

lateinit var appConfig: AppConfig
