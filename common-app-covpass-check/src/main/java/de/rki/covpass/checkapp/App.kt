/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp

import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.appVersion
import de.rki.covpass.checkapp.errorhandling.ErrorHandler
import de.rki.covpass.commonapp.CommonApplication
import de.rki.covpass.commonapp.dependencies.CommonDependencies
import de.rki.covpass.commonapp.dependencies.commonDeps

/**
 * Application class of CovPass Check.
 */
@OptIn(DependencyAccessor::class)
public class App : CommonApplication() {

    override fun onCreate() {
        super.onCreate()
        commonDeps = object : CommonDependencies() {
            override val errorHandler = ErrorHandler()
        }
        start()
        initializeKronosClock()
    }

    private fun initializeKronosClock() {
        commonDeps.kronosClock.syncInBackground()
    }

    override fun getAppVariantAndVersion(): String = "CovPassCheckApp/$appVersion"
}
