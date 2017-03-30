# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Andy\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-dontwarn okio.**
-dontwarn retrofit2.**

-keepattributes SourceFile,LineNumberTable,Signature,*Annotation*

-keep class com.google.gson.** { *; }
-keep class sun.misc.Unsafe { *; }

-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keep class com.mvp.weather_example.model.** { *; }

-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }

-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

-repackageclasses

-keep class com.mvp.MvpEventListener {
    public static final ** get(...);
 }
-keepnames class * extends com.mvp.MvpPresenter
-keepnames class * implements com.mvp.IMvpPresenter
-keep class **Proxy extends com.mvp.MvpPresenter {
    *** <init>(...);
}