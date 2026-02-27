# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class nl.designlama.pakkiepakkie.**$$serializer { *; }
-keepclassmembers class nl.designlama.pakkiepakkie.** {
    *** Companion;
}
-keepclasseswithmembers class nl.designlama.pakkiepakkie.** {
    kotlinx.serialization.KSerializer serializer(...);
}
