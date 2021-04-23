package com.ibm.health.vaccination.app.vaccinee.main

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import com.ibm.health.vaccination.app.vaccinee.common.ToggleFavoriteUseCase
import kotlinx.coroutines.CoroutineScope

class CertificateState(scope: CoroutineScope) : BaseState<BaseEvents>(scope) {

    fun onFavoriteClick(certId: String) {
        launch {
            ToggleFavoriteUseCase().toggleFavorite(certId)
        }
    }
}
