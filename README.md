# android-vaccination-app

This repo contains the vaccination app and commonly needed modules for Kotlin + Android.

The most important modules are:

* android-utils: Base classes for ViewModel, State, etc.
* android-utils-test: Utils for unit testing State, ViewModel, etc.
* annotations: Useful annotations/interfaces, e.g. for preventing R8/ProGuard obfuscation of JSON classes.
* dependency-versions-bom: Defines a common set of dependency versions, so there won't be any conflicts.
* gradle: Common infrastructure for linters, code coverage, R8/ProGuard.
* gson: Gson default settings with detection for missing annotations which would be problematic with R8/ProGuard.
* http: Ktor and OkHttp base clients with correct security configuration and Gson integration.
* logging: Simple wrapper (`Lumber`) around Timber which allows for full R8/ProGuard obfuscation.
* navigation: A simple activity and fragment based navigation system that uses `@Parcelize` to safely define arguments easily.

## SDK Installation

It's important that you include the BOM via `platform`. Those are the only modules which should have a version number. All other modules will automatically get the correct version from the BOM.

```
dependencies {
    api platform("com.ibm.health.common:dependency-versions-bom:$version")

    // No version needed here
    implementation 'com.ibm.health.common:http'
    implementation 'com.ibm.health.common:logging'
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
interface MyEvents : BaseEvents {
    fun onSomethingHappened()
}

class MyState(scope: CoroutineScope) : BaseState<MyEvents>(scope) {
    val data = MutableStateFlow<List<Entity>>(emptyList())

    fun refreshData() {
        launch {
            // If an exception is thrown here it'll automatically get caught trigger BaseFragment.onError(exception)
            data.value = requestLatestData()
        }
    }

    // You can also compose states
    val otherState by buildState { OtherState(scope) }

    // A contrived event example to get the point across
    init {
        otherState.subscribeToEvent {
            // Tell UI that something happened
            eventNotifier { onSomethingHappened() }
        }
    }
}

class MyFragment : BaseFragment(), MyEvents {
    // buildState creates a ViewModel to hold the state instance
    val state by buildState { MyState(scope) }

    override fun onSomethingHappened() {
        // handle event
    }
}
```
