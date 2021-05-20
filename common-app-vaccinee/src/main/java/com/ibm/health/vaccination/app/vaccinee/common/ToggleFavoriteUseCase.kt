/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.vaccination.app.vaccinee.common

import com.ibm.health.vaccination.app.vaccinee.storage.CertRepository

/**
 * Updates the favorite Vaccination certificate id in [CertRepository]
 */
internal class ToggleFavoriteUseCase(private val certRepository: CertRepository) {

    suspend fun toggleFavorite(certId: String) {
        certRepository.certs.update {
            it.toggleFavorite(certId)
        }
    }
}
