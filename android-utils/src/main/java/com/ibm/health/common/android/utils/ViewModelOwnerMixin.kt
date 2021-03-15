package com.ibm.health.common.android.utils

import androidx.lifecycle.LifecycleOwner
import com.ensody.reactivestate.android.handleEvents

/** Mixin interface for fragments/activities that have a ViewModel. */
public interface ViewModelOwnerMixin<T : BaseEvents> : OnCreateHook, ViewModelOwner<T>, LifecycleOwner {

    /** Initializes ViewModel handling. This should be called from within the constructor. */
    @Suppress("UNCHECKED_CAST")
    override fun onCreateHook() {
        super.onCreateHook()
        watchLoading(viewModel.isLoading, ::setLoading)
        viewModel.eventNotifier.handleEvents(this as T, this)
    }

    public fun setLoading(isLoading: Boolean)
}
