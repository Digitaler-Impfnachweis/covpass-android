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
import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.dependencies.sdkDeps
import kotlinx.coroutines.CoroutineScope

/**
 * Interface to communicate events from [VaccinationQRScannerViewModel] to [VaccinationQRScannerFragment].
 */
internal interface VaccinationQRScannerEvents : BaseEvents {
    fun onScanSuccess(certificateId: GroupedCertificatesId)
}

/**
 * ViewModel holding the business logic for decoding the Vaccination Certificate.
 */
internal class VaccinationQRScannerViewModel(
    scope: CoroutineScope,
    store: StateFlowStore,
) : BaseState<VaccinationQRScannerEvents>(scope) {

    val lastCertificateId by store.getData<GroupedCertificatesId?>(null)

    fun onQrContentReceived(qrContent: String) {
        launch {
            val vaccinationCertificate = sdkDeps.qrCoder.decodeVaccinationCert(qrContent)
            val certsFlow = covpassDeps.certRepository.certs
            var certId: GroupedCertificatesId? = null
            certsFlow.update {
                certId = it.addNewCertificate(
                    CombinedCovCertificate(vaccinationCertificate, qrContent)
                )
            }
            certId?.let {
                lastCertificateId.value = it
                eventNotifier {
                    onScanSuccess(it)
                }
            }
        }
    }
}
