package com.ibm.health.vaccination.app.vaccinee.main

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import com.ibm.health.vaccination.app.vaccinee.dependencies.vaccineeDeps
import kotlinx.coroutines.CoroutineScope

internal class CertificateState(scope: CoroutineScope) : BaseState<BaseEvents>(scope) {

    fun onFavoriteClick(certId: String) {
        launch {
            vaccineeDeps.toggleFavoriteUseCase.toggleFavorite(certId)
        }
    }
}
