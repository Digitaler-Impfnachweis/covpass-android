/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import android.content.Intent

public interface OnNewIntentListener {
    public fun onNewIntent(intent: Intent)
}
