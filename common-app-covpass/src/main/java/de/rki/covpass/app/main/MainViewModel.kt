/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.main

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import de.rki.covpass.app.dependencies.covpassDeps
import kotlinx.coroutines.CoroutineScope

/**
 * ViewModel providing the [onPageSelected] function and holding the [selectedCertId].
 */
internal class MainViewModel(scope: CoroutineScope) : BaseState<BaseEvents>(scope) {

    var selectedCertId: String? = null

    fun onPageSelected(position: Int) {
        selectedCertId = covpassDeps.certRepository.certs.value.getSortedCertificates()[position].getMainCertId()
    }
}
