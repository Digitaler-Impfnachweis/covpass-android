/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.scanner

import com.ensody.reactivestate.StateFlowStore
import com.ensody.reactivestate.getData
import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.sdk.android.cert.models.CombinedVaccinationCertificate
import de.rki.covpass.sdk.android.dependencies.sdkDeps
import kotlinx.coroutines.CoroutineScope
import java.lang.IllegalStateException

/**
 * Interface to communicate events from [VaccinationQRScannerViewModel] to [VaccinationQRScannerFragment].
 */
internal interface VaccinationQRScannerEvents : BaseEvents {
    fun onScanSuccess(certificateId: String)
}

/**
 * ViewModel holding the business logic for decoding the Vaccination Certificate.
 */
internal class VaccinationQRScannerViewModel(
    scope: CoroutineScope,
    store: StateFlowStore,
) : BaseState<VaccinationQRScannerEvents>(scope) {

    val lastCertificateId by store.getData<String?>(null)

    fun onQrContentReceived(qrContent: String) {
        launch {
            val vaccinationCertificate = sdkDeps.qrCoder.decodeVaccinationCert(qrContent)
            val certsFlow = covpassDeps.certRepository.certs
            certsFlow.update {
                it.addCertificate(
                    CombinedVaccinationCertificate(vaccinationCertificate, qrContent)
                )
            }
            val groupedCert = certsFlow.value.getGroupedCertificates(vaccinationCertificate.vaccination.id)
                ?: throw IllegalStateException("No GroupedCertificate found.")
            val mainCertId = groupedCert.getMainCertId()
            lastCertificateId.value = mainCertId
            eventNotifier {
                onScanSuccess(mainCertId)
            }
        }
    }
}
