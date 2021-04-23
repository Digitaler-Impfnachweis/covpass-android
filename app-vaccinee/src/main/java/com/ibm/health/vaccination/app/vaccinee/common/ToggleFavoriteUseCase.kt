package com.ibm.health.vaccination.app.vaccinee.common

import com.ibm.health.vaccination.app.vaccinee.storage.Storage

class ToggleFavoriteUseCase {

    suspend fun toggleFavorite(certId: String) {
        Storage.certCache.update {
            it.toggleFavorite(certId)
        }
        Storage.setVaccinationCertificateList(Storage.certCache.value)
    }
}
