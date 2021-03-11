# Add global library-only common R8/ProGuard rules here.
# The visibility-based rules are defined in proguard-rules-android-library-visibility.pro.
# Also see proguard-rules.pro for additional common rules.

# ---------------------------------------------------------------------------------------------------------------
# Taken from official documentation example for library authors:
# https://www.guardsquare.com/en/products/proguard/manual/examples
# ---------------------------------------------------------------------------------------------------------------
-keepparameternames
-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}
# ---------------------------------------------------------------------------------------------------------------
# End of official example
# ---------------------------------------------------------------------------------------------------------------
