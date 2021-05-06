package com.ibm.health.vaccination.app.vaccinee.main

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import com.ibm.health.vaccination.app.vaccinee.dependencies.vaccineeDeps
import kotlinx.coroutines.CoroutineScope

class MainState(scope: CoroutineScope) : BaseState<BaseEvents>(scope) {

    var selectedCertId: String? = null

    fun onPageSelected(position: Int) {
        selectedCertId = vaccineeDeps.certRepository.certs.value.getSortedCertificates()[position].getMainCertId()
    }
}
