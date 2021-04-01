package com.ibm.health.vaccination.app

import com.ibm.health.common.vaccination.app.CommonApplication
import com.ibm.health.vaccination.app.dependencies.CertCheckerDependencies
import com.ibm.health.vaccination.app.dependencies.certCheckerDeps

class App : CommonApplication() {

    override fun onCreate() {
        super.onCreate()
        certCheckerDeps = object : CertCheckerDependencies() {}
    }
}
