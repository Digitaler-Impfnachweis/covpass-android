/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import android.view.KeyEvent
import com.ibm.health.common.annotations.Abortable

public interface KeyEventListener {
    public fun dispatchKeyEvent(event: KeyEvent): Abortable
}
