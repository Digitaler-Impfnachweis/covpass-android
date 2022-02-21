# kotlinx serialization
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations
-keep,includedescriptorclasses class **$$serializer { *; }
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class de.rki.covpass.sdk.ticketing.TicketingType** { *; }
