package com.ibm.health.common.android.utils

import androidx.lifecycle.LifecycleOwner

/** Common interface for activities/fragments holding a [BaseViewModel]. */
public interface ViewModelOwner<T : BaseEvents> : LifecycleOwner {
    public val viewModel: BaseViewModel<T>
}
