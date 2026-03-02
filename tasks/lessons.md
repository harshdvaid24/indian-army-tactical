# Watch Face Development - Lessons Learned

## API & Architecture
- Use `androidx.wear.watchface` (Jetpack) not deprecated `WatchFaceService` from support lib
- CanvasType.HARDWARE for better perf on most Wear OS devices
- Keep `render()` under 16ms — avoid allocations in draw loop
- Pre-compute paths and paints in `init` or `onSurfaceChanged`

## Design
- Ambient mode: max 5% lit pixels for OLED burn-in protection
- Always support round AND square displays (use `screenBounds`)
- Military time = 24h format, standard for tactical watches

## Build
- Min SDK 30 for Wear OS 3+, target SDK 34
- `com.google.android.wearable` manifest metadata required

## Mistakes to Avoid

### Font files
- **Placeholder TTF = blank time display.** WFF silently drops a `<PartText>` whose `Font` fails to load — the whole element renders nothing with no error in logcat. Always `file font.ttf` to confirm it's a real TrueType binary before building.
- **WFF font reference syntax**: use `family="@font/dseg7"` (resource reference), NOT bare `family="dseg7"`. The bare name works in some emulators but fails on real devices.

### Manifest service entry (critical for picker visibility)
- **A WFF package without a `<service>` declaration is invisible.** The watch face picker (on-device carousel) discovers watch faces by scanning for services that:
  1. Export `android.service.wallpaper.WallpaperService` action
  2. Have category `com.google.android.wearable.watchface.category.WATCH_FACE`
  3. Hold `android.permission.BIND_WALLPAPER` permission
  Without this, `pm install` says "Success" but the face never appears in Settings.
- The correct service class for WFF packages is `com.google.wear.watchface.runtime.WatchFaceStubService` (the WFF runtime's stub; the package itself has `hasCode=false`).

### Signing
- Can't `adb install -r` over a package signed with a different keystore — must uninstall first, or use the same key from day one.
- Keep `gradle.properties` (with keystore credentials) out of public VCS.

### Build cache
- Gradle caches aggressively. After changing `build.gradle.kts` or `AndroidManifest.xml`, run `./gradlew clean` or the APK may retain old values even after a successful `assembleRelease`.
