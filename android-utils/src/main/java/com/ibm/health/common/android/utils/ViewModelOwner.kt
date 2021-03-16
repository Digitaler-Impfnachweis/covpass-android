package com.ibm.health.common.android.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ensody.reactivestate.EventNotifier
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/** Common interface for activities/fragments holding a [BaseViewModel]. */
public interface ViewModelOwner<T : BaseEvents> : LifecycleOwner {
    public val viewModel: BaseViewModel<T>
}

