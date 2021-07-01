# android-covpass-app

This repo contains the CovPass app and commonly needed modules for Kotlin + Android.

The most important modules are:

* android-utils: Useful lifecycle-aware helpers for view bindings, ViewPager2, etc.
* annotations: Useful annotations and marker interfaces, e.g. for preventing R8/ProGuard obfuscation.
* gradle: Common infrastructure for linters, code coverage, R8/ProGuard.
* covpass-http: Ktor and OkHttp base clients with correct security configuration.
* covpass-logging: Simple wrapper (`Lumber`) around Timber which allows for full R8/ProGuard obfuscation.
* navigation: A simple, yet flexible activity and fragment based navigation system that uses `@Parcelize` to safely define arguments easily. This solution is most useful when building SDKs and modularizing your code.
* covpass-bom: Our BOM - a common set of dependency versions, so there won't be any conflicts.
* covpass-sdk: The main CovPass SDK for Android.
* covpass-sdk-demo: Use this to override the SDK settings for the demo environment.

The apps live in these modules:

* common-app: Code shared between CovPass and CovPass Check.
* common-app-covpass: The CovPass app's code.
  * app-covpass-demo: The demo variant of the CovPass app.
  * app-covpass-prod: The production variant of the CovPass app.
* common-app-covpass-check: The CovPass Check app's code.
  * app-covpass-check-demo: The demo variant of the CovPass Check app.
  * app-covpass-check-prod: The production variant of the CovPass Check app.

Note: We explicitly avoid using flavors because they are problematic in many ways. They cause unnecessary Gradle scripts complexity for even non-trivial customizations, they interact badly with module substitution, the variant switcher doesn't work properly in all situations, switching flavors takes time (whereas apps in modules can be switched and launched directly), etc. Our experience at IBM has been much smoother since we threw away all flavors and switched to using modules.

## App architecture

The architecture in this project is based on our experience with significantly larger projects at IBM.
At a certain project size, you start to see which patterns break down and turn into bug sources and time wasters.
So, expect to see some patterns that might look unusual at first sight.

Our project uses a library based architecture.
In other words, the internal structure follows the same principle as when utilizing third-party dependencies.
We explicitly don't break with the library abstraction within the app by introducing framework hollywood principles.

We avoid unnecessary indirections and abstraction layers as long as a simple combination of the IDE's refactoring operations can trivially introduce those layers later.

We use lifecycle-aware, reactive, demand-driven programming. See [UI, reactivity, error handling, events](#ui-reactivity-error-handling-events) for more details and sample code.

Important architectural concerns and pitfalls that have to be taken care of in the whole codebase are abstracted away instead of plastering the code with error-prone copy-paste logic.
This includes error handling, correct lifecycle handling, and avoiding memory leaks - i.e. even trivial things like setting view bindings to null in `onDestroyView`.
Everything is automated as much as possible behind simple APIs, so mistakes become less likely and complexity is kept low outside of these helpful abstractions/APIs.
As long as you follow these APIs you're on the safe side, avoiding Android's pitfalls.

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
* It provides scoped dependencies. => That's just a factory function taking e.g. a `Fragment`.

In other words, Kotlin already provides everything you need for handling DI. So, we use pure, code-based DI.

* It's checked at compile-time and even the IDE immediately marks errors for you, so you don't have to wait for the compiler.
* It works without code generation, so builds are faster and you're never in an inconsistent state (e.g. classes getting marked as red because they can't be found).
* The error messages are clear and understandable.
* You can explore the DI graph trivially with your normal IDE (find usages, go to definition, unused deps appear as gray text, etc.).
* This solution works the same for libraries and internally within the app (we follow the library architecture, see above).
* The learning curve is minimal and in our experience, every junior developer just "gets it" without much thought.

In our much larger projects at IBM this solution has proven to work significantly better than Koin or Dagger.

### UI, reactivity, error handling, events

In order to automatically deal with Android's architecture details and pitfalls we utilize `ReactiveState`/`BaseReactiveState` subclasses which can be used for (multiplatform) ViewModels or stateful UseCases.
Internally, the instances live on an Android `ViewModel` (having the desired lifecycle), but they can be composed and tested more easily and in theory they allow reuse in multiplatform projects (though that's just a minor aspect in this app).

A `ReactiveState` comes with an `eventNotifier` to communicate events out-of-band (e.g. outside of the Fragment's lifecycle).
Also, `ReactiveState` provides a `launch` method to launch coroutines with automatic error handling.
Any errors are automatically forwarded to the UI via `eventNotifier` and trigger the Fragment's `onError(error: Throwable)` method.
Typically you'd use `MutableStateFlow` (or the mutation-optimized `MutableValueFlow`) in order to provide observable values.

A ViewModel / `ReactiveState` implementation can be attached to a Fragment using `by reactiveState` which is lifecycle-aware.

Simple example:

```kotlin
// BaseEvents defines our always-available events. Currently this only contains
// onError(error: Throwable).
// We use interfaces instead of sealed classes to represent events because that is more
// composable (almost like union types) and results in less boilerplate.
interface MyEvents : BaseEvents {
    fun onSomethingHappened(result: String)
}

// Usually the scope is passed from outside (in our case this will be the viewModelScope).
class MyViewModel(scope: CoroutineScope) : BaseReactiveState<MyEvents>(scope) {
    val data = MutableStateFlow<List<Entity>>(emptyList())

    fun refreshData() {
        // This launches a coroutine, catches any exceptions and forwards them via
        // eventNotifier { onError(error) }
        // and activates the `loading` state (unless you pass withLoading = null).
        launch {
            // If an exception is thrown here it'll automatically get caught and trigger
            // MyFragment.onError(exception)
            data.value = requestLatestData()
        }
    }

    // You can also compose states. The otherState.eventNotifier and otherState.loading
    // will get merged into MyViewModel.
    val otherState by childReactiveState { OtherReactiveState(scope) }

    // A contrived event example to get the point across
    fun doSomething() {
        launch {
            val result: String = someBackendCommunication()

            // Tell UI the result of doSomething (let's pretend this must be a one-time
            // executed event e.g. showing a dialog)
            eventNotifier { onSomethingHappened(result) }
        }
    }
}

// The fragment has to implement the events interface.
class MyFragment : BaseFragment(), MyEvents {
    // "by reactiveState" internally creates an Android ViewModel to hold the MyViewModel instance.
    // MyViewModel's eventNotifier and loading are automatically processed in a
    // lifecycle-aware way during the >= STARTED state.
    // The events are triggered as method calls on this fragment - e.g. onError(throwable).
    // Whenever `loading` changes, this triggers setLoading(isLoading: Boolean).
    val viewModel by reactiveState { MyViewModel(scope) }  // here, scope is an alias for viewModelScope

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Here we observe the data and update the UI whenever it changes by using autoRun.
        // The autoRun block will re-execute whenever viewModel.data is changed.
        // The get() call tells autoRun to get the viewModel.data.value and marks viewModel.data
        // as a dependency of the autoRun block.
        // This principle also works with multiple get() calls and can even be
        // used together with if/when-branches to track some dependencies only under certain
        // conditions (we even utilize this in our app - avoiding complicated Flow constructs).
        // Moreover, autoRun is lifecycle-aware and only executes in the >= STARTED state.
        autoRun {
            updateUI(get(viewModel.data))
        }
    }

    private fun updateUI(data: List<Entity>) {
        // ...
    }

    override fun onSomethingHappened(result: String) {
        // handle result event
    }
}
```

Note, the equivalent of

```kotlin
autoRun {
    updateUI(get(viewModel.data))
}
```

is more or less this block of code:

```kotlin
lifecycleScope.launchWhenStarted {
    viewModel.data.collect {
        try {
            updateUI(it)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            onError(e)
        }
    }
}
```

This was just the trivial case with a single `StateFlow`.
Imagine how complex things can become in the multi-`StateFlow` case combined with `if`/`when` and demand-driven resource allocation.
We want to avoid this repetitive complexity/boilerplate and prevent common mistakes like forgetting to give `CancellationException` a special treat.

### View Bindings

Use the `by viewBinding` helper which takes care of the whole lifecycle handling for you:

```kotlin
class DetailFragment : BaseFragment() {
    private val binding by viewBinding(DetailBinding::inflate)
}
```

With a single line of code, the binding is automatically inflated in `onCreateView` and cleared in `onDestroyView`, so you can avoid the whole boilerplate.

## Custom FragmentStateAdapter

Use `BaseFragmentStateAdapter` which automatically avoids memory leaks.
If that doesn't work for you, at least use `Fragment.attachViewPager`.

## Custom Toolbars

Use `Fragment.attachToolbar` to have automatically correct lifecycle handling and to avoid memory leaks.

## Screen navigation

Some of the modules in this repo were taken from our internal IBM projects.
The code-based navigation system is one of them.
Among the IBM developers who have worked with Android's Navigation component the experience was more on the negative side.
Especially when creating SDKs and modularizing your code, the Navigation component can have its pitfalls, runtime crashes and it can get in your way.
So, instead of using the Navigation components, we created a very simple code-based navigation system.

In this project we don't utilize the full flexibility of the navigation system.
When we started out we simply wanted to build on the same infrastructure that we were already most familiar with and not risk regretting the decision to use the Navigation components.
Moreover, this project is supposed to become a set of SDKs - one of them providing flexible, partial integration of the UI and navigation subgraphs.

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

// For passing results you define an interface and popUntil a fragment that
// implements the interface
interface DetailFragmentListener { fun onDetailResult(value: String) }
findNavigator().popUntil<DetailFragmentListener>()?.onDetailResult(someValue)

// triggers onBackPressed()
triggerBackPress()
```

So, the API is similar to the Navigation component, but everything is code-based (no XML) and thus easier to reason about and you get more control.

## License

```
Copyright (C) 2021 IBM Deutschland GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
