# Common Gradle modules

This is our standard set of Gradle modules for all repositories.

Please keep these modules as generic as possible because we want to copy the whole folder 1:1 between projects.
Modifications can be applied as overrides in your module's build.gradle and as
task aliases (task + deps) in case you use flavors.

Most important files:

* `ibmconfig.gradle`: Base IBM-specific config (Nexus credentials, Maven repos, etc.)
* `android-library.gradle`: Base setup for an Android library, including R8 obfuscation support.
* `android-app.gradle`: Base setup for an Android app, including R8 obfuscation support.
* `kotlin-library.gradle`: Base setup for a Kotlin (non-Android) library.
