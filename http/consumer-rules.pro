# Android has a hidden required method which is accessed via reflection.
# Let's just expose all public methods to be on the safe side.
-keepclassmembers class * implements javax.net.ssl.X509TrustManager {
    public <methods>;
}
