/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import kotlinx.coroutines.CoroutineScope

/** Common interface for all classes holding a CoroutineScope. */
public interface CoroutineScopeOwner {
    public val launcherScope: CoroutineScope
}
