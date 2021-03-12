# Never inline, make sure this is fully kept for debugging. In release builds we can still strip Lumber calls anyway.
-keep class com.ibm.health.common.logging.** { *; }
