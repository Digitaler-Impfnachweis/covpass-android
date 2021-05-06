package com.ibm.health.vaccination.app.certchecker

import com.ibm.health.common.vaccination.app.CommonApplication
import com.ibm.health.common.vaccination.app.dependencies.CommonDependencies
import com.ibm.health.common.vaccination.app.dependencies.commonDeps
import com.ibm.health.vaccination.app.certchecker.errorhandling.ErrorHandler

class App : CommonApplication() {

    override fun onCreate() {
        super.onCreate()
        commonDeps = object : CommonDependencies() {
            override val errorHandler = ErrorHandler()
        }
    }
}
