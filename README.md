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

The architecture in this project is based on our experience with significantly larger projects at IBM.
At a certain project size, you start to see which patterns break down and turn into bug sources and time wasters.
So, expect to see some patterns that might look unusual at first sight.

Our project uses a library based architecture.
In other words, the internal structure follows the same principle as when utilizing third-party dependencies.
We explicitly don't break with the library abstraction within the app by introducing framework hollywood principles.

We avoid unnecessary indirections and abstraction layers as long as a simple combination of the IDE's refactoring operations can trivially introduce those layers later.

We use lifecycle-aware, reactive, demand-driven programming. See [UI, reactivity, events](#ui-reactivity-events) for more details and sample code.

### Dependency injection

We are explicitly not using Dagger or Koin or any other framework for DI.

* Dagger is much too complicated and messy and the documentation is almost non-existent.
* Koin follows the (dynamic) service locator pattern which has been criticized for a very long time.
  * It's only checked at runtime, so it can lead to runtime crashes if dependencies are missing.
  * It's very difficult to navigate the graph because you can't use "go to definition" or "find usages".
  * In practice, in much larger projects at IBM, Koin had to be removed from the whole codebase because of this.
* Hilt is better than the other two, but still too complicated and limited for our projects.

All of these tools have a non-trivial learning curve in comparison to what they're doing.

What is a DI framework doing, anyway?

* It's (internally) defining a global variable that holds the dependencies. => That's just a global variable.
* It provides lazy singletons. => That's just `by lazy`.
* It provides factories. => That's just a function.
* It provides scoped dependencies. => That's just a factory function taking e.g. a `Context`.

In other words, Kotlin already provides everything you need for handling DI. So, we use pure, code-based DI.

* It's checked at compile-time.
* The compile-time error messages are clear and understandable.
* You can explore the DI graph trivially with your normal IDE (find usages, go to definition, unused deps appear as gray text, etc.).
* This solution works the same for libraries and internally within the app (we follow the library architecture, see above).
* The learning curve is minimal and in our experience, every junior developer just "gets it" without much thought.

This even allows much more flexibility e.g. by implementing demand-driven singletons that get destroyed when nobody uses them anymore (which can be important for security or resource usage).
None of the existing DI frameworks provides a solution for that.

In our much larger projects at IBM this solution has proven to work significantly better than Koin or Dagger.

### UI, reactivity, events

In order to automatically deal with Android's architecture we utilize `State` objects which are like ViewModels (or stateful UseCases/Interactors), but are independent of Android's `ViewModel` class and can be composed more easily.
A `State` comes with an `eventNotifier` to communicate events out-of-band (e.g. outside of the fragment's lifecycle) and a `launch` method to launch coroutines with automatic error handling.
Any errors are automatically forwarded to the UI via `eventNotifier` and trigger the fragment's `onError(error: Throwable)` method.
Typically you'd use `MutableStateFlow` (or the mutation-optimized `MutableValueFlow`) in order to provide observable values.

Simple example:

```kotlin
// BaseEvents defines our always-available events. Currently this only contains
// onError(error: Throwable).
// We use interfaces instead of sealed classes to represent events because that is more
// composable (like union types) and results in less boilerplate.
interface MyEvents : BaseEvents {
    fun onSomethingHappened(result: String)
}

// Usually the scope is passed from outside (in our case this will be the viewModelScope).
class MyState(scope: CoroutineScope) : BaseState<MyEvents>(scope) {
    val data = MutableStateFlow<List<Entity>>(emptyList())

    fun refreshData() {
        // This launches a coroutine, catches any exceptions and forwards them via
        // eventNotifier { onError(error) }
        // and activates the `isLoading` state (unless you pass withLoading = false).
        launch {
            // If an exception is thrown here it'll automatically get caught trigger
            // BaseFragment.onError(exception)
            data.value = requestLatestData()
        }
    }

    // You can also compose states. The otherState.eventNotifier and otherState.isLoading
    // will get merged into MyState.
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
    // buildState internally creates a ViewModel to hold the state instance.
    // The state's eventNotifier and isLoading are automatically processed.
    // The events are triggered as method calls on this fragment - e.g. onError(throwable).
    // Whenever isLoading changes this triggers setLoading(isLoading: Boolean).
    val state by buildState { MyState(scope) }  // here, scope is an alias for viewModelScope

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Observe the data and update the UI whenever it changes.
        // The autoRun block will re-execute whenever state.data is changed.
        // This principle also works with multiple get() calls and can even be
        // used together with if/when-branches to track some dependencies only under certain
        // conditions (we even utilize this in our code).
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

## Screen navigation

Some of the modules in this repo were taken from our internal IBM projects.
The code-based navigation system is one of them.
Among the IBM developers who have worked with Android's Navigation component the experience was more on the negative side.
Also, in our other internal projects we have more complex requirements.
So, instead of using the Navigation components we came up with a very simple code-based navigation system.

In this project we don't utilize the full flexibility of the navigation system.
When we started out we simply wanted to build on the same infrastructure that we were already most familiar with and not risk regretting the decision to use the Navigation components.

This is how you define a navigation point and access the arguments:

```kotlin
@Parcelize
class DetailFragmentNav(val certId: String) : FragmentNav(DetailFragment::class)

class DetailFragment : BaseFragment() {
    private val args: DetailFragmentNav by lazy { getArgs() }

    // ...

    // Within Fragments you can optionally customize the back button behavior
    override fun onBackPressed(): Abortable {
        if (something) {
            customBackPressLogic()
            return Abort  // aborts the default behavior
        }
        return Continue  // continue with default back behavior
    }
}
```

This is how you navigate:

```kotlin
findNavigator().push(DetailFragmentNav(args.certId))
findNavigator().pop()
findNavigator().popAll()
findNavigator().popUntil(SomeFragment::class)
triggerBackPress() // triggers onBackPressed()
```

So, the API is similar to the Navigation component, but everything is code-based (no XML) and thus easier to reason about and you get more control.
