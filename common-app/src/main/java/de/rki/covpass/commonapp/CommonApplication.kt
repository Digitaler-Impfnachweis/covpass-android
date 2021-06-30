/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp

import android.app.Application
import android.webkit.WebView
import androidx.fragment.app.FragmentActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.ibm.health.common.android.utils.AndroidDependencies
import com.ibm.health.common.android.utils.androidDeps
import com.ibm.health.common.android.utils.isDebuggable
import com.ibm.health.common.navigation.android.*
import com.ibm.health.common.securityprovider.initSecurityProvider
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.utils.DSC_UPDATE_INTERVAL_HOURS
import de.rki.covpass.commonapp.utils.DscListUpdater
import de.rki.covpass.http.HttpLogLevel
import de.rki.covpass.http.httpConfig
import de.rki.covpass.logging.Lumber
import de.rki.covpass.sdk.cert.toTrustedCerts
import de.rki.covpass.sdk.dependencies.SdkDependencies
import de.rki.covpass.sdk.dependencies.sdkDeps
import java.util.concurrent.TimeUnit

/** Common base application with some common functionality like setting up logging. */
public abstract class CommonApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // IMPORTANT: The security provider has to be initialized before anything else
        initSecurityProvider()

        if (isDebuggable) {
            Lumber.plantDebugTreeIfNeeded()
            httpConfig.enableLogging(HttpLogLevel.HEADERS)
            WebView.setWebContentsDebuggingEnabled(true)
        }

        navigationDeps = object : NavigationDependencies() {
            override val application = this@CommonApplication
            override val defaultScreenOrientation = Orientation.PORTRAIT
            override val animationConfig = DefaultNavigationAnimationConfig(250)
        }
        androidDeps = object : AndroidDependencies() {
            private val activityNavigator = ActivityNavigator()

            override val application: Application = this@CommonApplication

            override fun currentActivityOrNull(): FragmentActivity? =
                activityNavigator.getCurrentActivityOrNull() as? FragmentActivity
        }
        sdkDeps = object : SdkDependencies() {
            override val application: Application = this@CommonApplication
        }
    }

    public fun start() {
        sdkDeps.validator.updateTrustedCerts(commonDeps.dscRepository.dscList.value.toTrustedCerts())

        val tag = "dscListWorker"
        val dscListWorker: PeriodicWorkRequest =
            PeriodicWorkRequest.Builder(DscListUpdater::class.java, DSC_UPDATE_INTERVAL_HOURS, TimeUnit.HOURS)
                .addTag(tag)
                .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            tag,
            ExistingPeriodicWorkPolicy.KEEP,
            dscListWorker,
        )
    }
}
