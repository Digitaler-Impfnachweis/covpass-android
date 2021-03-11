# android-vaccination-app

This repo contains the vaccination app and commonly needed modules for Kotlin + Android.

The most important modules are:

* android-utils: Main entry point (only depend on this module). Public APIs which have to be delivered with source code (inline fun, typedef, etc.) and may depend on the minified source.
  * android-utils-minified: Imported indirectly. Base classes for ViewModel, State, etc. Depends on android-utils-unminified.
* android-utils-test: Utils for unit testing State, ViewModel, etc.
* annotations: Useful annotations/interfaces, e.g. for preventing R8/ProGuard obfuscation of JSON classes.
* dependency-versions-bom: Defines a common set of dependency versions, so there won't be any conflicts.
* gradle: Common infrastructure for linters, code coverage, R8/ProGuard. Most of our repos share the same copy of this folder.
* gson: Gson default settings with detection for missing annotations which would be problematic with R8/ProGuard.
* http: Ktor and OkHttp base clients with correct security configuration and Gson integration.
* logging: Simple wrapper (`Lumber`) around Timber which allows for full R8/ProGuard obfuscation.
* reactivestate-...: A direct copy of the [ReactiveState](https://github.com/ensody/ReactiveState-Kotlin) library (so in the worst case we can maintain this copy as a fork ourselves). In order to upgrade just copy the src folders from the original repo.

## Installation

It's important that you include the BOM via `platform`. Those are the only modules which should have a version number. All other modules will automatically get the correct version from the BOM.

```
dependencies {
    api platform("com.ibm.health.common:dependency-versions-bom:$commonKotlinAndroidVersion")

    // No version needed here
    implementation 'com.ibm.health.common:http'
    implementation 'com.ibm.health.common:logging'
}
```

## Logging

Use our logging module and the `Lumber` (e.g. `Lumber.d { "Some $value" }`) instead of `Timber`.

Only enable logging for debug builds and ideally only place this in `Application.onCreate()`:

```kotlin
if (BuildConfig.DEBUG) {
    Lumber.plantDebugTreeIfNeeded()
    WebView.setWebContentsDebuggingEnabled(true)
    httpConfig.enableLogging(HttpLogLevel.HEADERS)
}
```

This strange-looking lambda/interface based logging wrapper around Timber should be used instead of Timber because
we're able to abuse R8/ProGuard to fully strip any logging-related calls from release builds.

Why isn't this possible for e.g. `Timber.d("Some value ${value.something()})`?

1. You can strip the `Timber.d()` call itself with `-assumenosideeffects`, but this still leaves the string argument in the
   code because that's often constructed with StringBuilder.
2. You can strip unnecessary StringBuilder calls with `-assumenoexternalsideeffects` and `-assumenoexternalreturnvalues`.
3. PROBLEM: You CANNOT automatically strip the call to value.something() and other interpolated attributes.
   This only works for trivial cases like inserting a normal variable.
   Any other logging strings are left untouched in the resulting bytecode and this means we keep logging information
   which can be used for reverse-engineering.

So, the solution is to pass a lambda function which can be identified as a logging block and stripped as a whole.

## Gradle: R8, coverage, ktlint, detekt, ...

In order to keep the maintenance overhead small and ensure a correct setup, all our repos share a copy of the same `gradle` folder which defines many reusable rules in `gradle/common`.

These rules define our R8 setup, Maven repos, code coverage, linter setup, etc.

IMPORTANT: Some of these files are part of our admission, so even if they look trivial, changes to these files can be important!
