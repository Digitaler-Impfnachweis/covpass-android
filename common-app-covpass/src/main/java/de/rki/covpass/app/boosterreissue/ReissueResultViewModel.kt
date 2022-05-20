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
import de.rki.covpass.sdk.cert.models.ReissueType
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.reissuing.ReissuingRepository
import de.rki.covpass.sdk.reissuing.local.CertificateReissueExecutionType
import de.rki.covpass.sdk.storage.CertRepository
import kotlinx.coroutines.CoroutineScope

internal interface ReissueResultEvents : BaseEvents {
    fun onReissueFinish(cert: CovCertificate, groupedCertificatesId: GroupedCertificatesId)
    fun onDeleteOldCertificateFinish()
}

internal class ReissueResultViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val listCertIds: List<String>,
    private val reissueType: ReissueType,
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
            val action = if (reissueType == ReissueType.Booster) {
                CertificateReissueExecutionType.RENEW
            } else {
                CertificateReissueExecutionType.EXTEND
            }
            val reissuedCertificate = reissuingRepository.reissueCertificate(
                certificates = qrContents,
                action = action
            ).certificate

            val covCertificate = qrCoder.decodeCovCert(qrContent = reissuedCertificate, allowExpiredCertificates = true)

            CovPassCertificateStorageHelper.addNewCertificate(
                certRepository.certs,
                covCertificate,
                reissuedCertificate
            )?.let { groupedCertificateId ->
                certRepository.certs.update { groupedCertificateList ->
                    if (reissueType == ReissueType.Booster) {
                        groupedCertificateList.certificates.find { it.id == groupedCertificateId }?.finishedReissued(
                            listCertIds.first()
                        )
                    }
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
