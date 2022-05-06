/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.main

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.MutableValueFlow
import com.ibm.health.common.android.utils.BaseEvents
import kotlinx.coroutines.CoroutineScope

internal class MainViewModel constructor(
    scope: CoroutineScope,
) : BaseReactiveState<BaseEvents>(scope) {

    var isTwoGPlusOn = MutableValueFlow(false)
    var isTwoGPlusBOn = MutableValueFlow(false)
}
