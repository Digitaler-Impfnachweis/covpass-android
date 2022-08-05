/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.ticketing

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.http.util.HostPatternWhitelist
import de.rki.covpass.sdk.dependencies.sdkDeps
import kotlinx.coroutines.CoroutineScope

public class ConsentInitializationTicketingViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val hostPatternWhitelist: HostPatternWhitelist = sdkDeps.hostPatternWhitelist,
) : BaseReactiveState<BaseEvents>(scope) {

    public fun isWhitelisted(url: String): Boolean =
        hostPatternWhitelist.isWhitelisted(url)
}
