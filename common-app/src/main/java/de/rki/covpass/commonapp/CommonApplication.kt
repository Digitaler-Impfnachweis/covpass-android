/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp

import android.app.Application
import android.webkit.WebView
import androidx.fragment.app.FragmentActivity
import androidx.work.*
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.AndroidDependencies
import com.ibm.health.common.android.utils.androidDeps
import com.ibm.health.common.android.utils.isDebuggable
import com.ibm.health.common.navigation.android.*
import com.ibm.health.common.securityprovider.initSecurityProvider
import com.instacart.library.truetime.TrueTime
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.truetime.CustomCache
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
@OptIn(DependencyAccessor::class)
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

    public fun initializeTrueTime() {
        Thread {
            runBlocking {
                retry {
                    TrueTime
                        .build()
                        .withNtpHost(DE_NTP_HOST)
                        .withConnectionTimeout(10000)
                        .withCustomizedCache(CustomCache())
                        .initialize()
                }
                commonDeps.timeValidationRepository.validate()
            }
        }.start()
    }

    private fun prepopulateDb() {
        runBlocking {
            if (sdkDeps.covPassRulesRepository.getAllCovPassRules().isNullOrEmpty()) {
                sdkDeps.covPassRulesRepository.prepopulate(
                    sdkDeps.bundledRules
                )
            }
            if (sdkDeps.covPassValueSetsRepository.getAllCovPassValueSets().isNullOrEmpty()) {
                sdkDeps.covPassValueSetsRepository.prepopulate(
                    sdkDeps.bundledValueSets
                )
            }
            if (sdkDeps.covPassBoosterRulesRepository.getAllBoosterRules().isNullOrEmpty()) {
                sdkDeps.covPassBoosterRulesRepository.prepopulate(
                    sdkDeps.bundledBoosterRules
                )
            }
        }
    }

    private companion object {
        private const val DE_NTP_HOST = "1.de.pool.ntp.org"
    }
}
