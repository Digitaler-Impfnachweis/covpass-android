# android-vaccination-app

This repo contains the vaccination app and commonly needed modules for Kotlin + Android.

The most important modules are:

* android-utils: Base classes for ViewModel, State, etc.
* android-utils-test: Utils for unit testing State, ViewModel, etc.
* annotations: Useful annotations/interfaces, e.g. for preventing R8/ProGuard obfuscation of JSON classes.
* gradle: Common infrastructure for linters, code coverage, R8/ProGuard.
* gson: Gson default settings with detection for missing annotations which would be problematic with R8/ProGuard.
* http: Ktor and OkHttp base clients with correct security configuration and Gson integration.
* logging: Simple wrapper (`Lumber`) around Timber which allows for full R8/ProGuard obfuscation.
* navigation: A simple activity and fragment based navigation system that uses `@Parcelize` to safely define arguments easily.
* vaccination-bom: Defines a common set of dependency versions, so there won't be any conflicts.
* vaccination-sdk-android: The main vaccination SDK for Android.
* vaccination-sdk-android-demo: Use this to override the SDK settings for the demo environment.

## SDK Installation

It's important that you include the BOM via `platform`. Those are the only modules which should have a version number. All other modules will automatically get the correct version from the BOM.

```
dependencies {
    api platform("com.ibm.health.vaccination:vaccination-bom:$version")

    // No version needed here
    implementation 'com.ibm.health.vaccination:vaccination-sdk-android'
}
```

## App architecture

### UI and State

In order to automatically deal with Android's architecture we utilize `State` objects which are like stateful UseCases/ViewModels, but are independent of Android's `ViewModel` class and can be composed more easily.
A `State` comes with an `eventNotifier` to communicate events out-of-band (e.g. outside of the fragment's lifecycle) and a `launch` method to launch coroutines with automatic error handling.
Any errors are automatically forwarded to the UI via `eventNotifier` and trigger the fragment's `onError(error: Throwable)` method.
Typically you'd use `MutableStateFlow` (or the mutation-optimized `MutableValueFlow`) in order to provide observable values.

Simple example:

```kotlin
// BaseEvents defines our always-available events. Currently this only contains onError(error: Throwable).
// We use interfaces instead of sealed classes to represent events because that is more composable (like union types)
// and results in less boilerplate.
interface MyEvents : BaseEvents {
    fun onSomethingHappened(result: String)
}

// Usually the scope is passed from outside (in our case this will be the viewModelScope).
class MyState(scope: CoroutineScope) : BaseState<MyEvents>(scope) {
    val data = MutableStateFlow<List<Entity>>(emptyList())

    fun refreshData() {
        // This launches a coroutine, catches any exceptions and forwards them via eventNotifier { onError(error) }
        // and activates the `isLoading` state (unless you pass withLoading = false).
        launch {
            // If an exception is thrown here it'll automatically get caught trigger BaseFragment.onError(exception)
            data.value = requestLatestData()
        }
    }

    // You can also compose states. The otherState.eventNotifier and otherState.isLoading will get merged into MyState.
    val otherState by buildState { OtherState(scope) }

    // A contrived event example to get the point across
    fun doSomething() {
        launch {
            val result: String = someBackendCommunication()

            // Tell UI the result of doSomething
            eventNotifier { onSomethingHappened(result) }
        }
    }
}

// The fragment has to implement the events interface.
class MyFragment : BaseFragment(), MyEvents {
    // buildState internally creates a ViewModel to hold the state instance. The state's eventNotifier and isLoading
    // are automatically processed. The events are triggered as method calls on this fragment.
    // Whenever isLoading changes this triggers setLoading(isLoading: Boolean).
    val state by buildState { MyState(scope) }  // here, scope is an alias for the viewModelScope

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Observe the data and update the UI whenever it changes. The autoRun block will re-execute whenever
        // state.data is changed.
        autoRun {
            updateUI(get(state.data))
        }
    }

    fun updateUI(data: List<Entity>) {
        // ...
    }

    override fun onSomethingHappened(result: String) {
        // handle result event
    }
}
```
