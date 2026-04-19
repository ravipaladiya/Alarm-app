# Keep Room generated code
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.**

# Keep kotlinx.serialization metadata
-keepclasseswithmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
}
-keep,includedescriptorclasses class com.alarmapp.**$$serializer { *; }
-keepclassmembers class com.alarmapp.** {
    *** Companion;
}
-keepclasseswithmembers class com.alarmapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}
