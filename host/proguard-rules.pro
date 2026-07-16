# fix can't resolve issues
-dontwarn org.bouncycastle.**

# kotlinx serialization stuff
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault,PermittedSubclasses
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.livewire.**$$serializer { *; }
-keep @kotlinx.serialization.Serializable class com.livewire.**

# crypto stuff
-keep class dev.whyoleg.cryptography.** { *; }

# coil3
-keep class * implements coil3.util.FetcherServiceLoaderTarget { *; }
-keep class * implements coil3.util.DecoderServiceLoaderTarget { *; }

