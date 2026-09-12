# ProGuard rules for TPlus_JFFZ
# Keep model classes for Gson serialization
-keep class com.example.tplus_jffz.data.model.** { *; }
# Keep Room entities and DAOs
-keep class com.example.tplus_jffz.data.db.** { *; }
# Keep Retrofit interfaces
-keepclassmembers interface * {
    @retrofit2.http.* <methods>;
}
