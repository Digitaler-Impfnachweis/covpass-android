/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.federalstate

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.commonapp.dependencies.CommonDependencies
import de.rki.covpass.commonapp.dependencies.commonDeps
import kotlinx.coroutines.CoroutineScope

public interface ChangeFederalStateEvents : BaseEvents {
    public fun onUpdateDone()
}

public class ChangeFederalStateViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val commonDependencies: CommonDependencies = commonDeps,
) : BaseReactiveState<ChangeFederalStateEvents>(scope) {

    public fun updateFederalState(regionId: String) {
        launch {
            commonDependencies.federalStateRepository.federalState.set(regionId)
            eventNotifier {
                onUpdateDone()
            }
        }
    }
}
