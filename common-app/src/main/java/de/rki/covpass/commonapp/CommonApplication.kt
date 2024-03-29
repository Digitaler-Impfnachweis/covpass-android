/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp

import android.app.Application
import android.os.Build
import android.webkit.WebView
import androidx.fragment.app.FragmentActivity
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.AndroidDependencies
import com.ibm.health.common.android.utils.androidDeps
import com.ibm.health.common.android.utils.isDebuggable
import com.ibm.health.common.navigation.android.ActivityNavigator
import com.ibm.health.common.navigation.android.DefaultNavigationAnimationConfig
import com.ibm.health.common.navigation.android.NavigationDependencies
import com.ibm.health.common.navigation.android.Orientation
import com.ibm.health.common.navigation.android.navigationDeps
import com.ibm.health.common.securityprovider.initSecurityProvider
import de.rki.covpass.http.HttpLogLevel
import de.rki.covpass.http.httpConfig
import de.rki.covpass.logging.Lumber
import de.rki.covpass.sdk.cert.toTrustedCerts
import de.rki.covpass.sdk.dependencies.SdkDependencies
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.storage.RulesUpdateRepository.Companion.CURRENT_LOCAL_DATABASE_VERSION
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
        httpConfig.setUserAgent(
            "${getAppVariantAndVersion()} " +
                "($packageName; Android ${Build.VERSION.SDK_INT})",
        )

        navigationDeps = object : NavigationDependencies() {
            override val application = this@CommonApplication
            override val defaultScreenOrientation = Orientation.SENSOR
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

    public abstract fun getAppVariantAndVersion(): String

    public fun start() {
        sdkDeps.validator.updateTrustedCerts(sdkDeps.dscRepository.dscList.value.toTrustedCerts())
    }

    private fun prepopulateDb() {
        runBlocking {
            if (sdkDeps.rulesUpdateRepository.localDatabaseVersion.value != CURRENT_LOCAL_DATABASE_VERSION) {
                sdkDeps.covPassEuRulesRepository.deleteAll()
                sdkDeps.covPassDomesticRulesRepository.deleteAll()
                sdkDeps.covPassValueSetsRepository.deleteAll()
                sdkDeps.covPassBoosterRulesRepository.deleteAll()
                sdkDeps.rulesUpdateRepository.updateLocalDatabaseVersion()
            }
            if (sdkDeps.covPassEuRulesRepository.getAllRules().isEmpty()) {
                sdkDeps.covPassEuRulesRepository.prepopulate(
                    sdkDeps.bundledEuRules,
                )
            }
            if (sdkDeps.covPassDomesticRulesRepository.getAllRules().isEmpty()) {
                sdkDeps.covPassDomesticRulesRepository.prepopulate(
                    sdkDeps.bundledDomesticRules,
                )
            }
            if (sdkDeps.covPassValueSetsRepository.getAllCovPassValueSets().isEmpty()) {
                sdkDeps.covPassValueSetsRepository.prepopulate(
                    sdkDeps.bundledValueSets,
                )
            }
            if (sdkDeps.covPassBoosterRulesRepository.getAllBoosterRules().isEmpty()) {
                sdkDeps.covPassBoosterRulesRepository.prepopulate(
                    sdkDeps.bundledBoosterRules,
                )
            }
        }
    }
}
