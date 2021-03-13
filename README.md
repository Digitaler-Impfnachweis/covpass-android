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
