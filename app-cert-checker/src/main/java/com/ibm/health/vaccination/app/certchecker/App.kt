package com.ibm.health.vaccination.app.certchecker

import com.ibm.health.common.vaccination.app.CommonApplication
import com.ibm.health.vaccination.app.certchecker.dependencies.CertCheckerDependencies
import com.ibm.health.vaccination.app.certchecker.dependencies.certCheckerDeps
import com.ibm.health.vaccination.app.certchecker.errorhandling.ErrorHandler
import com.ibm.health.vaccination.common.android.dependencies.CommonDependencies
import com.ibm.health.vaccination.common.android.dependencies.commonDeps

class App : CommonApplication() {

    override fun onCreate() {
        super.onCreate()
        certCheckerDeps = object : CertCheckerDependencies() {}
        commonDeps = object : CommonDependencies() {
            override val errorHandler = ErrorHandler()
        }
    }
}
