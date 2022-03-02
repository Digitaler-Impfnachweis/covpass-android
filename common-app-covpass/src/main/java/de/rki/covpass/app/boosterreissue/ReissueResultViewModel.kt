/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.boosterreissue

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.scanner.CovPassCertificateStorageHelper
import de.rki.covpass.sdk.cert.QRCoder
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.reissuing.ReissuingRepository
import de.rki.covpass.sdk.storage.CertRepository
import kotlinx.coroutines.CoroutineScope

internal interface ReissueResultEvents : BaseEvents {
    fun onReissueFinish(cert: CovCertificate, groupedCertificatesId: GroupedCertificatesId)
    fun onDeleteOldCertificateFinish()
}

internal class ReissueResultViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    listCertIds: List<String>,
    private val qrCoder: QRCoder = sdkDeps.qrCoder,
    private val certRepository: CertRepository = covpassDeps.certRepository,
    private val reissuingRepository: ReissuingRepository = sdkDeps.reissuingRepository
) : BaseReactiveState<ReissueResultEvents>(scope) {

    init {
        val list = mutableListOf<String>()
        listCertIds.forEach { certId ->
            certRepository.certs.value.getCombinedCertificate(certId)?.qrContent?.let {
                list.add(it)
            }
        }
        reissueCertificate(list)
    }

    private fun reissueCertificate(qrContents: List<String>) {
        launch {
            val reissuedCertificate = reissuingRepository.reissueCertificate(
                qrContents
            ).certificate

            val covCertificate = qrCoder.decodeCovCert(qrContent = reissuedCertificate)

            CovPassCertificateStorageHelper.addNewCertificate(
                certRepository.certs,
                covCertificate,
                reissuedCertificate
            )?.let { groupedCertificateId ->
                certRepository.certs.update { groupedCertificateList ->
                    groupedCertificateList.certificates.find { it.id == groupedCertificateId }?.finishedReissued()
                }
                eventNotifier {
                    onReissueFinish(covCertificate, groupedCertificateId)
                }
            }
        }
    }

    fun deleteOldCertificate(certId: String) {
        certRepository.certs.value.deleteCovCertificate(certId).let {
            eventNotifier {
                onDeleteOldCertificateFinish()
            }
        }
    }
}
