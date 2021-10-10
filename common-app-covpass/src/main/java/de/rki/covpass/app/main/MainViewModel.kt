/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.main

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.sdk.cert.BoosterRulesValidator
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.storage.CertRepository
import kotlinx.coroutines.CoroutineScope

/**
 * ViewModel providing the [onPageSelected] function and holding the [selectedCertId].
 */
internal class MainViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val certRepository: CertRepository = covpassDeps.certRepository,
    private val boosterRulesValidator: BoosterRulesValidator = sdkDeps.boosterRulesValidator
) : BaseReactiveState<BaseEvents>(scope) {

    var selectedCertId: GroupedCertificatesId? = null

    fun onPageSelected(position: Int) {
        selectedCertId = certRepository.certs.value.getSortedCertificates()[position].id
    }
}
