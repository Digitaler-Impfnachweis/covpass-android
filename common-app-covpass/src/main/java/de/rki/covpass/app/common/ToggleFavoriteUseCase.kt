/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.common

import de.rki.covpass.app.storage.CertRepository

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
