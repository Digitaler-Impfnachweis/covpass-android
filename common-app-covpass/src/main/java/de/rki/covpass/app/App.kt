/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app

import de.rki.covpass.commonapp.CommonApplication
import de.rki.covpass.commonapp.dependencies.CommonDependencies
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.app.dependencies.CovpassDependencies
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.errorhandling.ErrorHandler

/**
 * Application class which defines dependencies for the Covpass App
 */
internal class App : CommonApplication() {

    override fun onCreate() {
        super.onCreate()
        covpassDeps = object : CovpassDependencies() {}
        commonDeps = object : CommonDependencies() {
            override val errorHandler = ErrorHandler()
        }
    }
}
