package com.ibm.health.vaccination.app.main

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import com.ibm.health.vaccination.app.storage.Storage
import kotlinx.coroutines.CoroutineScope

class CertificateState(scope: CoroutineScope) : BaseState<BaseEvents>(scope) {

    fun onFavoriteClick(certId: String) {
        launch {
            Storage.certCache.update {
                it.toggleFavorite(certId)
            }
            Storage.setVaccinationCertificateList(Storage.certCache.value)
        }
    }
}
