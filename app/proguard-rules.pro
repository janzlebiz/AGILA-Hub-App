# AGILA Hub ProGuard Rules

# Keep Ktor Client classes
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Keep Kotlinx Serialization classes and annotations
-keepattributes *Annotation*,ElementValuePairs*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
    @kotlinx.serialization.Serializable <fields>;
}
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class *$$serializer {
    *** INSTANCE;
}
-keepclassmembers class * {
    *** Companion;
}

# Keep Data Models and API DTOs
-keep class com.example.data.models.** { *; }
-keep class com.example.data.api.** { *; }
