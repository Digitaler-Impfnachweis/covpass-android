/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.misuseprevention

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.scanner.CovPassCertificateStorageHelper
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.storage.FederalStateRepository
import de.rki.covpass.sdk.cert.GStatusAndMaskValidator
import de.rki.covpass.sdk.cert.QRCoder
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.storage.CertRepository
import kotlinx.coroutines.CoroutineScope

/**
 * Interface to communicate events from [MisusePreventionViewModel] to [MisusePreventionFragment].
 */
internal interface MisusePreventionEvents : BaseEvents {
    fun onSaveSuccess(groupedCertificatesId: GroupedCertificatesId, certId: String)
}

/**
 * ViewModel holding the business logic for decoding and storing the [CovCertificate].
 */
internal class MisusePreventionViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val qrCoder: QRCoder = sdkDeps.qrCoder,
    private val certRepository: CertRepository = covpassDeps.certRepository,
    private val gStatusAndMaskValidator: GStatusAndMaskValidator = sdkDeps.gStatusAndMaskValidator,
    private val federalStateRepository: FederalStateRepository = commonDeps.federalStateRepository,
) : BaseReactiveState<MisusePreventionEvents>(scope) {

    fun addNewCertificate(qrContent: String) {
        launch {
            val covCertificate = qrCoder.decodeCovCert(qrContent, allowExpiredCertificates = true)
            CovPassCertificateStorageHelper.addNewCertificate(
                certRepository.certs,
                covCertificate,
                qrContent,
            )?.let {
                gStatusAndMaskValidator.validate(certRepository, federalStateRepository.federalState.value)
                eventNotifier {
                    onSaveSuccess(it, covCertificate.dgcEntry.id)
                }
            }
        }
    }
}
