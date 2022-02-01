/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.information

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.rules.CovPassDomesticRulesRepository
import de.rki.covpass.sdk.rules.CovPassEuRulesRepository
import de.rki.covpass.sdk.utils.DscListUpdater
import kotlinx.coroutines.CoroutineScope

internal class ManualUpdateViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val dscListUpdater: DscListUpdater = sdkDeps.dscListUpdater,
    private val covPassEuRulesRepository: CovPassEuRulesRepository = sdkDeps.covPassEuRulesRepository,
    private val covPassDomesticEuRulesRepository: CovPassDomesticRulesRepository =
        sdkDeps.covPassDomesticRulesRepository,
) : BaseReactiveState<BaseEvents>(scope) {

    fun updateRulesAndCertificates() {
        launch {
            // update certificates
            dscListUpdater.update()
            // update rules
            covPassEuRulesRepository.loadRules()
            covPassDomesticEuRulesRepository.loadRules()
        }
    }
}
