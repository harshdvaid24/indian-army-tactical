# Watch Face - keep service
-keep class com.watchforge.tacticalindia.TacticalWatchFaceService
-keep class * extends androidx.wear.watchface.WatchFaceService

# Keep complication rendering
-keep class androidx.wear.watchface.complications.** { *; }
