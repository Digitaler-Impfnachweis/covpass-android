package com.ibm.health.common.vaccination.app

import android.app.Application
import android.webkit.WebView
import androidx.fragment.app.FragmentActivity
import com.ibm.health.common.android.utils.AndroidDependencies
import com.ibm.health.common.android.utils.androidDeps
import com.ibm.health.common.android.utils.isDebuggable
import com.ibm.health.common.http.HttpLogLevel
import com.ibm.health.common.http.httpConfig
import com.ibm.health.common.logging.Lumber
import com.ibm.health.common.navigation.android.ActivityNavigator
import com.ibm.health.common.navigation.android.initActivityNavigator
import com.ibm.health.common.securityprovider.initSecurityProvider

public abstract class CommonApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // IMPORTANT: The security provider has to be initialized before anything else
        initSecurityProvider()

        if (isDebuggable) {
            Lumber.plantDebugTreeIfNeeded()
            // Make sure not to commit HttpLogLevel.BODY because that can break streaming requests
            httpConfig.enableLogging(HttpLogLevel.HEADERS)
            WebView.setWebContentsDebuggingEnabled(true)
        }

        initActivityNavigator(this)
        val activityNavigator = ActivityNavigator()
        androidDeps = object : AndroidDependencies() {
            override val application: Application = this@CommonApplication

            override fun currentActivityOrNull(): FragmentActivity? =
                activityNavigator.getCurrentActivityOrNull() as? FragmentActivity
        }
    }
}
