/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.app.countries.CountryResolver.deCountry
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.sdk.cert.GStatusValidator
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.storage.CertRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Interface to communicate events from [DgcEntryDetailViewModel] to [DgcEntryDetailFragment].
 */
internal interface DgcEntryDetailEvents : BaseEvents {
    fun onDeleteDone(isGroupedCertDeleted: Boolean)
}

/**
 * ViewModel to handle business logic related to [DgcEntryDetailFragment].
 */
internal class DgcEntryDetailViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val certRepository: CertRepository = covpassDeps.certRepository,
    private val gStatusValidator: GStatusValidator = sdkDeps.gStatusValidator,
) : BaseReactiveState<DgcEntryDetailEvents>(scope) {

    val isPdfExportEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isCertificateRevoked: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun onDelete(certId: String) {
        launch {
            var isGroupedCertDeleted = false
            certRepository.certs.update {
                isGroupedCertDeleted = it.deleteCovCertificate(certId)
            }
            gStatusValidator.validate(certRepository)
            eventNotifier {
                onDeleteDone(isGroupedCertDeleted)
            }
        }
    }

    fun checkPdfExport(certId: String) {
        val combinedCovCertificate =
            certRepository.certs.value.getCombinedCertificate(certId) ?: return
        isPdfExportEnabled.value =
            combinedCovCertificate.covCertificate.issuer.equals(
                deCountry.countryCode,
                ignoreCase = true,
            )
        isCertificateRevoked.value = combinedCovCertificate.isRevoked
    }
}
