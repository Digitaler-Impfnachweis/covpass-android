package com.ibm.health.common.android.utils

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import com.ensody.reactivestate.ErrorEvents
import com.ensody.reactivestate.EventNotifier
import com.ensody.reactivestate.android.buildViewModel
import com.ensody.reactivestate.android.handleEvents
import com.ensody.reactivestate.android.onCreate
import com.ensody.reactivestate.android.stateViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

public inline fun <reified E : ErrorEvents, reified T : BaseViewModel<E>, O> O.buildBaseViewModel(
    crossinline block: () -> T,
): Lazy<T> where O : Fragment, O : ErrorEvents, O : LoadingStateHook {
    require(this is E) { "You must implement the given ViewModel's events interface" }
    val lazyViewModel = buildViewModel(block)
    onCreate {
        isLoading.addLoadingState(lazyViewModel.value.isLoading)
        lazyViewModel.value.eventNotifier.handleEvents(this, this)
    }
    return lazyViewModel
}

public inline fun <reified E : ErrorEvents, reified T : BaseViewModel<E>, O> O.stateBaseViewModel(
    crossinline block: (SavedStateHandle) -> T,
): Lazy<T> where O : Fragment, O : ErrorEvents, O : LoadingStateHook {
    require(this is E) { "You must implement the given ViewModel's events interface" }
    val lazyViewModel = stateViewModel(block)
    onCreate {
        isLoading.addLoadingState(lazyViewModel.value.isLoading)
        lazyViewModel.value.eventNotifier.handleEvents(this, this)
    }
    return lazyViewModel
}

public inline fun <reified E : ErrorEvents, reified T : BaseViewModel<E>, O> O.buildBaseViewModel(
    crossinline block: () -> T,
): Lazy<T> where O : ComponentActivity, O : ErrorEvents, O : LoadingStateHook {
    require(this is E) { "You must implement the given ViewModel's events interface" }
    val lazyViewModel = buildViewModel(block)
    onCreate {
        isLoading.addLoadingState(lazyViewModel.value.isLoading)
        lazyViewModel.value.eventNotifier.handleEvents(this, this)
    }
    return lazyViewModel
}

public inline fun <reified E : ErrorEvents, reified T : BaseViewModel<E>, O> O.stateBaseViewModel(
    crossinline block: (SavedStateHandle) -> T,
): Lazy<T> where O : ComponentActivity, O : ErrorEvents, O : LoadingStateHook {
    require(this is E) { "You must implement the given ViewModel's events interface" }
    val lazyViewModel = stateViewModel(block)
    onCreate {
        isLoading.addLoadingState(lazyViewModel.value.isLoading)
        lazyViewModel.value.eventNotifier.handleEvents(this, this)
    }
    return lazyViewModel
}
