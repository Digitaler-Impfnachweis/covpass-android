-keepattributes *Annotation*,EnclosingMethod,Signature
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**

-keepclassmembers class * {
    *** Companion;
}
-keep public class * {
    *;
}
