# This is included in the common-kotlin-android "obfuscation" module's consumer rules, so our clients' apps
# usually have these rules applied anyway. You can explicitly import this rule if you have more strict requirements
# for your SDK and want to obfuscate as much as possible.

# ---------------------------------------------------------------------------------------------------------------
# Remove logging calls which expose too many important details
# ---------------------------------------------------------------------------------------------------------------
# Our own lambda-based logging library with full removal even of arbitrary string interpolation.
-assumenosideeffects class * implements com.ibm.health.common.logging.LogBlock {
    public ** invoke(...);
}
-assumenosideeffects class com.ibm.health.common.logging.Lumber$Companion {
    public void plantDebugTreeIfNeeded(...);
    public void v(...);
    public void i(...);
    public void w(...);
    public void wtf(...);
    public void d(...);
    public void e(...);
    public void println(...);
    public void log(...);
}

# Remove Timber and Android log calls. This won't remove arguments that use string interpolation.
# See proguard-rules.pro for removal of interpolation.
-assumenosideeffects class timber.log.Timber {
    public static void plant(...);
    public static void v(...);
    public static void i(...);
    public static void w(...);
    public static void wtf(...);
    public static void d(...);
    public static void e(...);
    public static void println(...);
    public static void log(...);
}
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int wtf(...);
    public static int d(...);
    public static int e(...);
    public static int println(...);
}
# ---------------------------------------------------------------------------------------------------------------
# End of logging removal
# ---------------------------------------------------------------------------------------------------------------
