/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.main

import com.ensody.reactivestate.BaseReactiveState
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import kotlinx.coroutines.CoroutineScope

/**
 * ViewModel providing the [onPageSelected] function and holding the [selectedCertId].
 */
internal class MainViewModel(scope: CoroutineScope) : BaseReactiveState<BaseEvents>(scope) {

    var selectedCertId: GroupedCertificatesId? = null

    fun onPageSelected(position: Int) {
        selectedCertId = covpassDeps.certRepository.certs.value.getSortedCertificates()[position].id
    }
}
