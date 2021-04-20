package com.ibm.health.vaccination.app.certchecker

import com.ibm.health.common.vaccination.app.CommonApplication
import com.ibm.health.vaccination.app.certchecker.dependencies.CertCheckerDependencies
import com.ibm.health.vaccination.app.certchecker.dependencies.certCheckerDeps

class App : CommonApplication() {

    override fun onCreate() {
        super.onCreate()
        certCheckerDeps = object : CertCheckerDependencies() {}
    }
}
