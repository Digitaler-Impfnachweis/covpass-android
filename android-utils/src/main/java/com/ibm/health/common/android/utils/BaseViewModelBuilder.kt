package com.ibm.health.common.android.utils

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import com.ensody.reactivestate.ErrorEvents
import com.ensody.reactivestate.android.buildViewModel
import com.ensody.reactivestate.android.handleEvents
import com.ensody.reactivestate.android.stateViewModel

public inline fun <reified E : ErrorEvents, reified T : BaseViewModel<E>, O> O.buildBaseViewModel(
    crossinline block: () -> T,
): Lazy<T> where O : Fragment, O : ErrorEvents, O : LoadingStateHook =
    _attachLazyViewModel(buildViewModel(block))

public inline fun <reified E : ErrorEvents, reified T : BaseViewModel<E>, O> O.stateBaseViewModel(
    crossinline block: (SavedStateHandle) -> T,
): Lazy<T> where O : Fragment, O : ErrorEvents, O : LoadingStateHook =
    _attachLazyViewModel(stateViewModel(block))

public inline fun <reified E : ErrorEvents, reified T : BaseViewModel<E>, O> O.buildBaseViewModel(
    crossinline block: () -> T,
): Lazy<T> where O : ComponentActivity, O : ErrorEvents, O : LoadingStateHook =
    _attachLazyViewModel(buildViewModel(block))

public inline fun <reified E : ErrorEvents, reified T : BaseViewModel<E>, O> O.stateBaseViewModel(
    crossinline block: (SavedStateHandle) -> T,
): Lazy<T> where O : ComponentActivity, O : ErrorEvents, O : LoadingStateHook =
    _attachLazyViewModel(stateViewModel(block))

@Suppress("FunctionName")
public inline fun <reified E : ErrorEvents, reified T : BaseViewModel<E>, O> O._attachLazyViewModel(
    lazyViewModel: Lazy<T>,
): Lazy<T> where O : LifecycleOwner, O : ErrorEvents, O : LoadingStateHook {
    require(this is E) { "You must implement the given ViewModel's events interface" }
    lifecycleScope.launchWhenCreated {
        isLoading.addLoadingState(lazyViewModel.value.isLoading)
        lazyViewModel.value.eventNotifier.handleEvents(this@_attachLazyViewModel, this@_attachLazyViewModel)
    }
    return lazyViewModel
}
