package com.ibm.health.common.android.utils.reactive

import kotlinx.coroutines.CoroutineScope

/** Common interface for all classes holding a CoroutineScope. */
public interface CoroutineScopeOwner {
    public val launcherScope: CoroutineScope
}
