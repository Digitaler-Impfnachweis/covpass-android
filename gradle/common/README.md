# Common Gradle modules

This is our standard set of Gradle modules for all repositories.

Please keep these modules as generic as possible because we want to copy the whole folder 1:1 between projects.
Modifications can be applied as overrides in your module's build.gradle and as
task aliases (task + deps) in case you use flavors.
