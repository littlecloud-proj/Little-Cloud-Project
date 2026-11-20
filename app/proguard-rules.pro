# Little Cloud — R8 rules for the release build.
# The libraries used (Compose, kotlinx.serialization, androidx) bring their own rules; the app reads
# its JSON data through JsonObject, without generated serializers, so nothing of its own needs keeping.

# Keep line numbers in crash reports; the mapping file in the bundle restores the names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
