# Keep Gson model classes (they are reflected over for (de)serialization).
-keep class com.example.snaprecipe.data.model.** { *; }

# Retrofit / OkHttp / Gson baseline rules.
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
