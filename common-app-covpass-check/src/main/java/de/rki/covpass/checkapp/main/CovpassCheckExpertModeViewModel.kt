/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.main

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ensody.reactivestate.derived
import com.ensody.reactivestate.get
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.checkapp.dependencies.covpassCheckDeps
import de.rki.covpass.checkapp.storage.CheckAppRepository
import de.rki.covpass.checkapp.storage.CheckingMode
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.storage.CheckContextRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

public class CovpassCheckExpertModeViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val checkAppRepository: CheckAppRepository = covpassCheckDeps.checkAppRepository,
    private val checkContextRepository: CheckContextRepository = commonDeps.checkContextRepository,
) : BaseReactiveState<BaseEvents>(scope) {

    public val isHintVisible: StateFlow<Boolean> = derived {
        get(checkAppRepository.activatedCheckingMode) != CheckingMode.Mode3G &&
            get(checkContextRepository.isExpertModeOn)
    }
}
