# ==============================================================================
# ProGuard / R8 Configuration for MewEnglish (Google Play Release)
# ==============================================================================

# --- Line numbers and crash debugging ---
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Preserve exception stack traces
-renamesourcefileattribute SourceFile

# --- Application Models & Business Logic ---
-keep class com.example.ngilizcequizapp.** { *; }
-keep interface com.example.ngilizcequizapp.** { *; }
-keep class navigation.** { *; }
-keep interface navigation.** { *; }

# Keep all Enums and their values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- JSON & Serialization (Gson) ---
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter { *; }
-keep class * implements com.google.gson.TypeAdapterFactory { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
    @com.google.gson.annotations.Expose <fields>;
}

# --- Kotlinx Serialization ---
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

# --- Supabase & Ktor Client ---
-keep class io.github.jan.supabase.** { *; }
-keep interface io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-keep interface io.ktor.** { *; }
-dontwarn io.ktor.**
-dontwarn io.github.jan.supabase.**

# --- ML Kit (Language ID & Translate) ---
-keep class com.google.mlkit.** { *; }
-keep interface com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# --- Lottie Compose Animations ---
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# --- Coil Image Loading ---
-keep class coil.** { *; }
-dontwarn coil.**

# --- AndroidX Media3 / ExoPlayer ---
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# --- AndroidX Compose & Lifecycle ---
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.lifecycle.** { *; }

# --- AndroidX Security & Crypto ---
-keepclassmembers class * extends androidx.security.crypto.MasterKey { *; }

# ==============================================================================
# Security: Strip all android.util.Log calls in Release Builds
# ==============================================================================
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int println(...);
}