# Watch Face Development - Project Plan

## 🎯 Current: Indian Army Digital Tactical Watch Face
**Style**: Casio G-Shock Digital Tactical
**Platform**: Jetpack Canvas API (Kotlin) → All Wear OS devices
**Theme**: Military rugged, high-visibility, Indian Army inspired

---

### Phase 1: Project Scaffold ✅
- [x] Android project setup (Gradle, dependencies, manifest)
- [x] WatchFaceService + CanvasRenderer skeleton
- [ ] Build verification (compiles clean) ← needs Android Studio

### Phase 2: Core Digital Display ✅
- [x] Segmented LCD-style font rendering (7-segment / custom)
- [x] Primary time display (HH:MM:SS, 24h military format)
- [x] Date display (DD-MMM-YYYY military style)
- [x] Day of week display
- [x] 24H indicator label

### Phase 3: Tactical UI Elements ✅
- [x] Rugged bezel/border frame (G-Shock inspired, 12 tick marks)
- [x] Indian tricolor accent lines (saffron/white/green)
- [x] "INDIAN ARMY" header branding
- [x] Bottom info panels (BATT/ALT/COMP)
- [x] "TACTICAL" footer
- [x] Color scheme: Military green (#4A5D23), Amber LCD (#FF8C00), Khaki accents

### Phase 4: Functional Complications ✅
- [x] Heart rate complication slot (id:100)
- [x] Step counter complication slot (id:101)
- [x] Battery level indicator (id:102, with color bar)
- [ ] Compass heading (if sensor available) ← Phase 6
- [ ] World time / dual timezone ← Phase 6

### Phase 5: Ambient & Interactive Modes ✅
- [x] Ambient mode (HH:MM only, minimal drawing, burn-in safe)
- [x] Active mode (full color, blinking colon, all panels)
- [ ] Tap interactions (cycle info panels) ← Phase 6
- [x] Mode-specific rendering paths

### Phase 6: Polish & Publish
- [x] Watch face preview/thumbnail generation (480×480 PNG, drawable-nodpi)
- [x] User style settings (accentColor: Amber / Green / Blue via Flavors)
- [x] Manifest service entry fixed (WATCH_FACE category + WatchFaceStubService)
- [x] Font fixed: real DSEG7 Classic Bold TTF replacing HTML placeholder
- [x] Release keystore generated (`tacticalindia-release.jks`)
- [x] Release APK + AAB built and deployed to emulator & Galaxy Watch
- [ ] Testing on round + square displays ← verify square Wear OS emulator
- [ ] Privacy policy URL (required for BODY_SENSORS permission on Play Store)
- [ ] Play Store listing assets: feature graphic (1024×500), 2× Wear OS screenshots
- [ ] Upload `IndianArmyTactical-v1.0.0-release.aab` to Google Play Console
- [ ] Performance optimization (<16ms frame time verification)

---

## 📋 Future Watch Faces Queue
- [ ] Analog Tactical (Luminox-style glow markers)
- [ ] Hybrid Military (analog hands + digital complications)
- [ ] Minimal Officer (clean, formal, Indian tricolor accent)

---

## Review Log

### 03-Mar-2026
- **Bug fix**: `dseg7.ttf` was an HTML stub — replaced with real DSEG7 Classic Bold v0.46 TTF. This was why the time display was completely blank.
- **Bug fix**: `family="dseg7"` in watchface.xml → `family="@font/dseg7"` (WFF requires `@font/` prefix for custom fonts).
- **Bug fix**: `AndroidManifest.xml` was missing `<service>` declaration with `android.service.wallpaper.WallpaperService` action + `WATCH_FACE` category. Without this, the watch face picker cannot discover the package — it simply doesn't appear in the list.
- **Play Store**: `compileSdk` bumped to 35, `minSdk` lowered to 30 (broader Wear OS 3+ reach), `versionCode=6`, `versionName=1.0.0`.
- **Signing**: Release keystore at `tacticalindia-release.jks`, credentials in `gradle.properties`.
- **Artifacts**: `IndianArmyTactical-v1.0.0-release.aab` (76 KB) ready in `~/Downloads`.
