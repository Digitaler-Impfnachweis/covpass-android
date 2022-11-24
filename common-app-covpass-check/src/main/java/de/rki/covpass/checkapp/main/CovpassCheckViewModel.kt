/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.main

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.storage.FederalStateRepository
import de.rki.covpass.sdk.cert.CovPassMaskRulesDateResolver
import de.rki.covpass.sdk.dependencies.sdkDeps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

public class CovpassCheckViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val covPassMaskRulesDateResolver: CovPassMaskRulesDateResolver = sdkDeps.covPassMaskRulesDateResolver,
    private val federalStateRepository: FederalStateRepository = commonDeps.federalStateRepository,
) : BaseReactiveState<BaseEvents>(scope) {

    public val maskRuleValidFrom: MutableStateFlow<String?> = MutableStateFlow(null)

    public fun onFederalStateChanged() {
        launch {
            maskRuleValidFrom.value = covPassMaskRulesDateResolver.getMaskRuleValidity(
                federalStateRepository.federalState.value.lowercase(),
            )
        }
    }

    init {
        onFederalStateChanged()
    }
}
