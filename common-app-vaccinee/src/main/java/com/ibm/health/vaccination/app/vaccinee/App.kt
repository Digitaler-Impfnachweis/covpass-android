package com.ibm.health.vaccination.app.vaccinee

import com.ibm.health.common.vaccination.app.CommonApplication
import com.ibm.health.common.vaccination.app.dependencies.CommonDependencies
import com.ibm.health.common.vaccination.app.dependencies.commonDeps
import com.ibm.health.vaccination.app.vaccinee.dependencies.VaccineeDependencies
import com.ibm.health.vaccination.app.vaccinee.dependencies.vaccineeDeps
import com.ibm.health.vaccination.app.vaccinee.errorhandling.ErrorHandler

/**
 * Application class which defines dependencies for the Vaccinee App
 */
internal class App : CommonApplication() {

    override fun onCreate() {
        super.onCreate()
        vaccineeDeps = object : VaccineeDependencies() {}
        commonDeps = object : CommonDependencies() {
            override val errorHandler = ErrorHandler()
        }
    }
}
