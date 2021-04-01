package com.ibm.health.vaccination.app

import com.ibm.health.common.vaccination.app.CommonApplication
import com.ibm.health.vaccination.app.dependencies.VaccineeDependencies
import com.ibm.health.vaccination.app.dependencies.vaccineeDeps

class App : CommonApplication() {

    override fun onCreate() {
        super.onCreate()
        vaccineeDeps = object : VaccineeDependencies() {}
    }
}
