# Keep kotlinx.serialization generated serializers for network DTOs.
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class com.krew18.app.data.remote.dto.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
