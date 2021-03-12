# This interface marks classes whose fields are reflected over at runtime. This is mostly used with JSON serialization.
-keepclassmembers,includedescriptorclasses class * implements com.ibm.health.common.annotations.KeepFields {
    !transient <fields>;
}
