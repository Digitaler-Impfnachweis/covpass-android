/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp

import de.rki.covpass.commonapp.CommonApplication
import de.rki.covpass.commonapp.dependencies.CommonDependencies
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.checkapp.errorhandling.ErrorHandler

/**
 * Application class of CovPass Check.
 */
public class App : CommonApplication() {

    override fun onCreate() {
        super.onCreate()
        commonDeps = object : CommonDependencies() {
            override val errorHandler = ErrorHandler()
        }
        start()
    }
}
