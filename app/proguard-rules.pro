# ProGuard & R8 Optimization Rules for QR Screen Scanner

# General optimization settings
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepattributes SourceFile,LineNumberTable

# Keep Room Database entities and DAO implementations
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep ML Kit & Google Play Services Vision classes
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.vision.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_barcode.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**

# Kotlin Coroutines & Dispatchers
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Jetpack Compose
-dontwarn androidx.compose.**
