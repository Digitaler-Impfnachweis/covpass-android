/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.sdk.storage.CertRepository
import de.rki.covpass.app.validitycheck.countries.CountryRepository.defaultCountry
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.Vaccination
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
) : BaseReactiveState<DgcEntryDetailEvents>(scope) {

    val isPdfExportEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun onDelete(certId: String) {
        launch {
            var isGroupedCertDeleted = false
            certRepository.certs.update {
                isGroupedCertDeleted = it.deleteCovCertificate(certId)
            }
            eventNotifier {
                onDeleteDone(isGroupedCertDeleted)
            }
        }
    }

    fun checkPdfExport(certId: String) {
        val combinedCovCertificate = certRepository.certs.value.getCombinedCertificate(certId) ?: return
        if (combinedCovCertificate.covCertificate.issuer.equals(defaultCountry.countryCode, ignoreCase = true)) {
            isPdfExportEnabled.value = when (combinedCovCertificate.covCertificate.dgcEntry) {
                is Vaccination, is Recovery -> true
                else -> false
            }
        } else {
            isPdfExportEnabled.value = false
        }
    }
}
