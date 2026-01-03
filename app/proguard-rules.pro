# Keep Room entities
-keep class com.example.diaryapp.data.database.entities.** { *; }
-keep @androidx.room.Entity class * { *; }

# Keep ML Kit models
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Keep serializeables
-keepclassmembers class * implements java.io.Serializable { *; }
