/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import android.os.Looper
import com.ensody.reactivestate.dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private val mainScope by lazy {
    MainScope()
}

public fun <T> runBlockingOnMainThread(block: () -> T): T =
    if (Looper.getMainLooper().thread != Thread.currentThread()) {
        runBlocking(dispatchers.main) {
            block()
        }
    } else {
        block()
    }

public fun runOnMainThread(block: () -> Unit) {
    if (Looper.getMainLooper().thread != Thread.currentThread()) {
        mainScope.launch {
            block()
        }
    } else {
        block()
    }
}
