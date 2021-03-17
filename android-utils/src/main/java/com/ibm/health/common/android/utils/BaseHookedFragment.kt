package com.ibm.health.common.android.utils

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

/** Base class that comes with hook support. */
public abstract class BaseHookedFragment(@LayoutRes contentLayoutId: Int = 0) :
    Fragment(contentLayoutId),
    LoadingStateHook,
    BaseEvents {

    override val isLoading: IsLoading = IsLoading()
}
