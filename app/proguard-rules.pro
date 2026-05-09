# Add project specific ProGuard rules here.
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Moshi
-keep class com.chronie.helperunited.model.** { *; }
-keepclassmembers class com.chronie.helperunited.model.** { *; }

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Hilt
-keep class dagger.hilt.** { *; }

# Keep source file info for stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
