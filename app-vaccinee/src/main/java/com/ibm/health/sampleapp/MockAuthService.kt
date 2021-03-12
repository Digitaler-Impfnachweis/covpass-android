package com.ibm.health.sampleapp

import android.content.Intent
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

object MockAuthService : HealthAuthService {
    val authcodes = mutableMapOf<String, String>()
    private var username = ""
    private var password = ""
    private val scope = MainScope()
    var lastLoginInMillis: Long = System.currentTimeMillis()

    override fun requestInsuranceAuthCode(
        forConfiguration: String,
        onComplete: (Result<String>) -> Unit
    ) {
        authcodes[forConfiguration]?.let { authCode ->
            onComplete(Result.success(authCode))
            authcodes.remove(forConfiguration)
        } ?: scope.launch {
            try {
                val authCode = appAuthConfig.sampleKeycloakApi.login(username, password)
                onComplete(Result.success(authCode))
                authcodes.remove(forConfiguration)
            } catch (e: LoginError) {
                onComplete(Result.failure(e))
                authcodes.clear()
            }
        }
    }

    override suspend fun refreshLoginIfNeeded(skipSessionRefresh: Boolean) {
    }

    override fun getUtcTimestampInMillisOfLastLogin(): Long = lastLoginInMillis

    override fun doReloginWithIntent(): Intent = MockHostLoginActivity.createIntent(
        appConfig.application
    )

    override fun requestInsuranceTokenIntent(
        forConfiguration: String,
        forceReauth: Boolean,
        onComplete: (Result<AuthResult>) -> Unit
    ) {
        onComplete(
            Result.success(
                AuthResultNavigation(
                    MockAlviTokenActivityNav(forConfiguration).toIntent(appConfig.application)
                )
            )
        )
    }

    /**
     * Cache credentials in member fields and call the login function of [SampleKeycloakApi].
     */
    suspend fun login(username: String, password: String): String {
        this.username = username
        this.password = password
        return appAuthConfig.sampleKeycloakApi.login(username, password)
    }
}
