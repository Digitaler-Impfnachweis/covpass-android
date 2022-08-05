/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.common

import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.storage.CertRepository

/**
 * Updates the favorite [GroupedCertificatesId] in [CertRepository]
 */
internal class ToggleFavoriteUseCase(private val certRepository: CertRepository) {

    suspend fun toggleFavorite(certId: GroupedCertificatesId) {
        certRepository.certs.update {
            it.toggleFavorite(certId)
        }
    }
}
