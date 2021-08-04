/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp

import android.app.Application
import android.webkit.WebView
import androidx.fragment.app.FragmentActivity
import androidx.work.*
import com.ibm.health.common.android.utils.AndroidDependencies
import com.ibm.health.common.android.utils.androidDeps
import com.ibm.health.common.android.utils.isDebuggable
import com.ibm.health.common.navigation.android.*
import com.ibm.health.common.securityprovider.initSecurityProvider
import de.rki.covpass.commonapp.utils.schedulePeriodicWorker
import de.rki.covpass.http.HttpLogLevel
import de.rki.covpass.http.httpConfig
import de.rki.covpass.logging.Lumber
import de.rki.covpass.sdk.cert.toTrustedCerts
import de.rki.covpass.sdk.dependencies.SdkDependencies
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.utils.*
import de.rki.covpass.sdk.worker.DscListWorker
import kotlinx.coroutines.runBlocking

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
        prepopulateDb()
    }

    public fun start() {
        sdkDeps.validator.updateTrustedCerts(sdkDeps.dscRepository.dscList.value.toTrustedCerts())
        initializeWorkManager(WorkManager.getInstance(this))
    }

    public open fun initializeWorkManager(workManager: WorkManager) {
        workManager.apply {
            schedulePeriodicWorker<DscListWorker>("dscListWorker")
        }
    }

    private fun prepopulateDb() {
        runBlocking {
            if (sdkDeps.rulesRepository.getAllRuleIdentifiers().isNullOrEmpty()) {
                sdkDeps.rulesRepository.prepopulate(
                    sdkDeps.bundledRuleIdentifiers,
                    sdkDeps.bundledRules
                )
            }
            if (sdkDeps.valueSetsRepository.getAllValueSetIdentifiers().isNullOrEmpty()) {
                sdkDeps.valueSetsRepository.prepopulate(
                    sdkDeps.bundledValueSetIdentifiers,
                    sdkDeps.bundledValueSets
                )
            }
        }
    }
}
