/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app

import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.appVersion
import de.rki.covpass.app.dependencies.CovpassDependencies
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.errorhandling.ErrorHandler
import de.rki.covpass.app.errorhandling.ReissueErrorHandler
import de.rki.covpass.commonapp.CommonApplication
import de.rki.covpass.commonapp.dependencies.CommonDependencies
import de.rki.covpass.commonapp.dependencies.commonDeps

/**
 * Application class which defines dependencies for the Covpass App
 */
@OptIn(DependencyAccessor::class)
internal class App : CommonApplication() {

    override fun onCreate() {
        super.onCreate()
        covpassDeps = object : CovpassDependencies() {
            override val reissueErrorHandler = ReissueErrorHandler()
        }
        commonDeps = object : CommonDependencies() {
            override val errorHandler = ErrorHandler()
        }
        start()
    }

    override fun getAppVariantAndVersion(): String = "CovPassApp/$appVersion"
}
