package com.ibm.health.vaccination.app.vaccinee.common

import com.ibm.health.vaccination.app.vaccinee.storage.Storage
import com.ibm.health.vaccination.sdk.android.dependencies.sdkDeps
import com.ibm.health.vaccination.sdk.android.qr.models.ExtendedVaccinationCertificate

class AddCertUseCase(private val storage: Storage) {

    suspend fun addCertFromQr(qrContent: String) {
        storage.certs.update {
            it.addCertificate(
                // FIXME replace validationQrContent with the simplified validation certificate
                ExtendedVaccinationCertificate(sdkDeps.qrCoder.decodeVaccinationCert(qrContent), qrContent, qrContent)
            )
        }
    }
}
