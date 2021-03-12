package com.ibm.health.sampleapp

/** The app auth configuration (and DI). */
interface AppAuthConfig {
    val sampleKeycloakApi: SampleKeycloakApi
}

lateinit var appAuthConfig: AppAuthConfig
