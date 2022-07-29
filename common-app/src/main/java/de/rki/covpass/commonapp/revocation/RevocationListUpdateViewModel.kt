/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.revocation

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.revocation.RevocationLocalListRepository
import kotlinx.coroutines.CoroutineScope

public class RevocationListUpdateViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val revocationLocalListRepository: RevocationLocalListRepository = sdkDeps.revocationLocalListRepository,
) : BaseReactiveState<BaseEvents>(scope) {

    public suspend fun update() {
        revocationLocalListRepository.update()
    }
}
