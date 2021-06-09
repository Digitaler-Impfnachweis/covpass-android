/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import de.rki.covpass.app.dependencies.covpassDeps
import kotlinx.coroutines.CoroutineScope

/**
 * Interface to communicate events from [DgcEntryDetailViewModel] to [DgcEntryDetailFragment].
 */
internal interface DgcEntryDetailEvents : BaseEvents {
    fun onDeleteDone(isGroupedCertDeleted: Boolean)
}

/**
 * ViewModel to handle business logic related to [DgcEntryDetailFragment].
 */
internal class DgcEntryDetailViewModel(scope: CoroutineScope) : BaseState<DgcEntryDetailEvents>(scope) {

    fun onDelete(certId: String) {
        launch {
            var isGroupedCertDeleted = false
            covpassDeps.certRepository.certs.update {
                isGroupedCertDeleted = it.deleteCovCertificate(certId)
            }
            eventNotifier {
                onDeleteDone(isGroupedCertDeleted)
            }
        }
    }
}
