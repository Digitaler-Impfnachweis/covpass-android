package com.ibm.health.vaccination.app.vaccinee.common

import com.ibm.health.vaccination.app.vaccinee.storage.Storage

class ToggleFavoriteUseCase(private val storage: Storage) {

    suspend fun toggleFavorite(certId: String) {
        storage.certs.update {
            it.toggleFavorite(certId)
        }
    }
}
