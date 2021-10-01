/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.information

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.sdk.cert.toTrustedCerts
import de.rki.covpass.sdk.dependencies.SdkDependencies
import de.rki.covpass.sdk.dependencies.sdkDeps
import kotlinx.coroutines.CoroutineScope

internal class AppUpdateViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val sdkDependencies: SdkDependencies = sdkDeps,
) : BaseReactiveState<BaseEvents>(scope) {

    fun updateRulesAndCertificates() {
        launch {
            // update certificates
            val result = sdkDependencies.dscListService.getTrustedList()
            val dscList = sdkDependencies.decoder.decodeDscList(result)
            sdkDependencies.validator.updateTrustedCerts(dscList.toTrustedCerts())
            sdkDependencies.dscRepository.updateDscList(dscList)

            // update rules
            sdkDependencies.covPassRulesRepository.loadRules()
        }
    }
}
