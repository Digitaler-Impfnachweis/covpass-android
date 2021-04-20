package com.ibm.health.vaccination.app.vaccinee

import com.ibm.health.common.vaccination.app.CommonApplication
import com.ibm.health.vaccination.app.vaccinee.dependencies.VaccineeDependencies
import com.ibm.health.vaccination.app.vaccinee.dependencies.vaccineeDeps

class App : CommonApplication() {

    override fun onCreate() {
        super.onCreate()
        vaccineeDeps = object : VaccineeDependencies() {}
    }
}
