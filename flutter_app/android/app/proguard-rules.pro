# ============================================================
# Saimum Music — ProGuard / R8 rules
# ============================================================

# --------------- Flutter & Dart ---------------
-keep class io.flutter.** { *; }
-keep class io.flutter.plugins.** { *; }
-dontwarn io.flutter.**

# --------------- Isar (database) ---------------
# Isar uses code generation and reflection for collection classes.
# Keep all generated schemas and the core library intact.
-keep class dev.isar.** { *; }
-keep class com.isar.** { *; }
-keep @dev.isar.isar_core.annotations.** class * { *; }
-keepclassmembers class * {
    @dev.isar.isar_core.annotations.Id *;
}
# Keep all Isar-generated .g.dart companion classes (accessed by name)
-keep class ** extends dev.isar.isar_core.IsarCollection { *; }
-dontwarn dev.isar.**

# --------------- encrypt / Pointy Castle (AES-256) ---------------
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**
-keep class com.nimbusds.** { *; }

# --------------- MediaKit / MPV ---------------
-keep class com.alexmercerind.** { *; }
-dontwarn com.alexmercerind.**

# --------------- AudioService ---------------
-keep class com.ryanheise.** { *; }
-dontwarn com.ryanheise.**

# --------------- Kotlin coroutines ---------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# --------------- OkHttp / Dio ---------------
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# --------------- General safety ---------------
# Prevent stripping serialisable/parcelable classes
-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
