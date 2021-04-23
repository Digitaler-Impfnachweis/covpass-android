package com.ibm.health.vaccination.app.vaccinee.main

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import com.ibm.health.vaccination.app.vaccinee.common.AddCertUseCase
import kotlinx.coroutines.CoroutineScope

class MainState(scope: CoroutineScope) : BaseState<BaseEvents>(scope) {

    fun onQrContentReceived(qrContent: String) {
        launch {
            AddCertUseCase().addCertFromQr(qrContent)
        }
    }
}
