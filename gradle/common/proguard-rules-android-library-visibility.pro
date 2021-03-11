# Add global library-only visibility-based R8/ProGuard rules here.
# The other library rules are defined in proguard-rules-android-library.pro.
# Also see proguard-rules.pro for additional common rules.

# ---------------------------------------------------------------------------------------------------------------
# Taken from official documentation example for library authors:
# https://www.guardsquare.com/en/products/proguard/manual/examples
# ---------------------------------------------------------------------------------------------------------------
-keep public class * {
    public protected *;
}
# ---------------------------------------------------------------------------------------------------------------
# End of official example
# ---------------------------------------------------------------------------------------------------------------
