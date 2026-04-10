# Watch Face Format v2 — Reusable Design Prompt

Use this file to bootstrap a new Wear OS watch face based on this codebase.
Copy the scaffold below, replace every `%%PLACEHOLDER%%` with your design tokens,
and paste the result into Copilot / any AI assistant.

---

## Prompt Template

```
I want to build a Wear OS watch face using Watch Face Format v2 (WFF v2, resource-only
XML — no Kotlin, hasCode=false, compileSdk=35, minSdk=30).

### Design name
%%WATCH_FACE_NAME%%

### Theme / concept
%%DESIGN_CONCEPT%%   (e.g. "military tactical dark green", "minimalist white", "neon cyberpunk")

### Canvas
450 × 450 px, round dial, backgroundColor: %%BG_COLOR%%

### Font for time
%%FONT%%   (e.g. "@font/dseg7" for 7-segment LCD, or "SYNC_TO_DEVICE" for system font)

### Layout sections (describe with rough y-ranges)
- y=%%TOP%%-%%TOP_END%%   Header / logo text
- y=%%MID%%-%%MID_END%%   Main time display (ACTIVE + AMBIENT)
- y=%%BOT%%-%%BOT_END%%   Bottom info panels (BATT, complications, etc.)
- y=%%FOOT%%-%%FOOT_END%% Footer text

### Color configurations (WFF ColorConfiguration, up to 3 index values each)
Name: %%CONFIG_ID%%   (e.g. "accentColor")
  Option %%OPT1_ID%%: primary=%%OPT1_COLOR0%%, secondary=%%OPT1_COLOR1%%, frame=%%OPT1_COLOR2%%
  Option %%OPT2_ID%%: ...

### Default colour option
%%DEFAULT_OPTION%%

### Background theme config (optional extra ColorConfiguration)
Name: bgTheme
  tactical: bg=%%TACTICAL_BG%%, panel=%%TACTICAL_PANEL%%, stroke=%%TACTICAL_STROKE%%
  stealth:  bg=%%STEALTH_BG%%,  panel=%%STEALTH_PANEL%%,  stroke=%%STEALTH_STROKE%%
  urban:    bg=%%URBAN_BG%%,    panel=%%URBAN_PANEL%%,    stroke=%%URBAN_STROKE%%

### Time display
- Formats: %%TIME_FORMATS%%   (e.g. "24H + 12H toggle via ListConfiguration")
- Ghost segments: %%GHOST_YES_NO%%   (YES = render "88:88:88" at alpha=18 below active time)
- Seconds: %%SECONDS%% (show/hide via BooleanConfiguration)

### Complications
Slot 100: %%COMP_100%%  (e.g. HEART_RATE SHORT_TEXT)
Slot 101: %%COMP_101%%  (e.g. STEP_COUNT SHORT_TEXT)
Slot 102: %%COMP_102%%  (e.g. user-configurable SHORT_TEXT/RANGED_VALUE)
Slot 103: %%COMP_103%%  (e.g. DAY_OF_WEEK SHORT_TEXT)

### Flavors (picker carousel combos)
Flavor %%FLAVOR1_ID%%: accentColor=%%FLAVOR1_ACCENT%%, bgTheme=%%FLAVOR1_BG%%
Flavor %%FLAVOR2_ID%%: ...

### Special elements (tick marks, tricolor lines, bezel, battery bar, etc.)
%%SPECIAL%%

### Ambient mode
- Show only: %%AMBIENT_ELEMENTS%%   (e.g. "monochrome HH:MM + date + subtitle")
- All active elements: Variant mode="AMBIENT" target="alpha" value="0"
- Ambient-only elements: alpha="0", Variant mode="AMBIENT" target="alpha" value="255"

### Package & signing
applicationId: %%APP_ID%%            (e.g. com.yourcompany.watchfacename)
versionCode: 1
versionName: 1.0.0
keystoreAlias: %%KEYSTORE_ALIAS%%
```

---

## Codebase Structure

```
indian-army-tactical/
├── app/
│   ├── build.gradle.kts              ← compileSdk=35, minSdk=34, AAB dex stripping
│   └── src/main/
│       ├── AndroidManifest.xml       ← hasCode=false, WFF v2 property, standalone=true
│       └── res/
│           ├── raw/watchface.xml     ← entire watch face design (Scene + UserConfigurations)
│           ├── xml/watch_face_info.xml ← FlavorsSupported, Editable, MultipleInstances
│           ├── font/dseg7.ttf        ← DSEG7 Classic Bold (7-segment LCD font)
│           ├── values/strings.xml    ← all configuration/flavor labels
│           └── drawable-nodpi/preview.png ← 450×450 picker preview bitmap
├── build.gradle.kts                  ← root project (plugins, repos)
├── tacticalindia-release.jks         ← release signing keystore
├── gradle.properties                 ← keystore credentials (gitignored)
└── tasks/
    ├── prompt.md                     ← this file
    ├── lessons.md                    ← WFF pitfalls & patterns
    └── todo.md                       ← development backlog
```

---

## Key WFF v2 Patterns

### Ghost LCD shadow segments
```xml
<!-- Ghost layer — always same position as active time, alpha ~7% -->
<PartText x="0" y="172" width="450" height="90" alpha="18">
    <Variant mode="AMBIENT" target="alpha" value="0" />
    <!-- hide when this variant shouldn't show -->
    <Condition expression="[CONFIGURATION.timeFormat] == 1"><Variant target="alpha" value="0" /></Condition>
    <Text align="CENTER">
        <Font family="@font/dseg7" size="68" weight="BOLD" color="[CONFIGURATION.accentColor.0]">
            <Template>88:88:88</Template>
        </Font>
    </Text>
</PartText>
```

### Dynamic battery bar
```xml
<!-- Track (dim background) -->
<PartDraw x="68" y="339" width="84" height="4">
    <Rectangle x="0" y="0" width="84" height="4"><Fill color="#22FFFFFF" /></Rectangle>
</PartDraw>
<!-- Fill (width = live expression) -->
<PartDraw x="68" y="339" width="84" height="4">
    <Rectangle x="0" y="0" width="[BATTERY_PERCENT]*84/100" height="4">
        <Fill color="[CONFIGURATION.accentColor.0]" />
    </Rectangle>
</PartDraw>
```

### Multi-condition visibility (AND logic)
Multiple `<Condition>` blocks inside one PartText/PartDraw AND together:
```xml
<PartText ...>
    <!-- hide in ambient -->
    <Variant mode="AMBIENT" target="alpha" value="0" />
    <!-- hide when timeFormat == 12h (index 1) -->
    <Condition expression="[CONFIGURATION.timeFormat] == 1"><Variant target="alpha" value="0" /></Condition>
    <!-- hide when showSeconds == FALSE (0) -->
    <Condition expression="[CONFIGURATION.showSeconds] == 0"><Variant target="alpha" value="0" /></Condition>
    ...
</PartText>
```

### DSEG7 font syntax (CRITICAL)
```xml
<!-- WRONG -->  <Font family="dseg7" ...>
<!-- RIGHT  -->  <Font family="@font/dseg7" ...>
```

### Ambient mode toggle pattern
```xml
<!-- Visible in ACTIVE, hidden in AMBIENT -->
<PartText ...>
    <Variant mode="AMBIENT" target="alpha" value="0" />
    ...
</PartText>

<!-- Hidden in ACTIVE, visible in AMBIENT -->
<PartText ... alpha="0">
    <Variant mode="AMBIENT" target="alpha" value="255" />
    ...
</PartText>
```

### WatchFaceStubService (CRITICAL for picker discovery)
```xml
<!-- In AndroidManifest.xml, inside <application> -->
<service
    android:name="com.google.wear.watchface.runtime.WatchFaceStubService"
    android:label="@string/watch_face_name"
    android:permission="android.permission.BIND_WALLPAPER"
    android:exported="true">
    <intent-filter>
        <action android:name="android.service.wallpaper.WallpaperService" />
        <category android:name="com.google.android.wearable.watchface.category.WATCH_FACE" />
    </intent-filter>
    <meta-data android:name="android.service.wallpaper"
               android:resource="@xml/watch_face" />
    <meta-data android:name="com.google.android.wearable.watchface.preview"
               android:resource="@drawable/preview" />
</service>
```

---

## Common Pitfalls & Solutions

| Issue | Cause | Fix |
|-------|-------|-----|
| Blank time display | Corrupt/missing TTF, or `family="dseg7"` (no `@font/` prefix) | Check font binary is real TTF; use `family="@font/dseg7"` |
| Watch face not in picker | Missing `<service>` in manifest | Add `WatchFaceStubService` with `WATCH_FACE` category |
| Signature mismatch on reinstall | Old debug APK vs new release APK | `adb uninstall <appId>` first |
| Build uses old APK | Gradle incremental cache | `./gradlew clean assembleRelease` |
| Resource shrinking removes watchface.xml | `isShrinkResources=true` | Set `isShrinkResources = false` in release buildType |
| Play Store rejects WFF app with `hasCode=true` | Kotlin code present when none needed | Set `hasCode="false"` in manifest `<application>` |

---

## Build & Deploy Commands

```bash
# ── Full clean build (APK + AAB) ──
cd indian-army-tactical
./gradlew clean :app:assembleRelease :app:bundleRelease

# Outputs:
#   APK → app/build/outputs/apk/release/app-release.apk
#   AAB → app/build/outputs/bundle/release/app-release.aab  (Play Store)
```

### Install on Wear OS Emulator

```bash
# 1. Start the Wear OS emulator
emulator -avd WearOS5_Watch &

# 2. Wait for boot
adb -s emulator-5556 wait-for-device
adb -s emulator-5556 shell 'while [ "$(getprop sys.boot_completed)" != "1" ]; do sleep 2; done'

# 3. Uninstall old version (clears DWF runtime cache)
adb -s emulator-5556 uninstall com.army.tectical 2>/dev/null

# 4. Install release APK
adb -s emulator-5556 install app/build/outputs/apk/release/app-release.apk

# 5. Wait ~15s for flavor processing, then verify
sleep 15
adb -s emulator-5556 logcat -d | grep "Finished adding flavors"
# Should show: Success count: 5, Failure count: 0

# 6. Select from picker: long-press home → swipe right → "+ Add" → scroll to "Indian Army T..."
```

### Install on Physical Watch (Sideload)

```bash
# 1. Enable ADB on watch: Settings → Developer options → ADB debugging
# 2. Connect via WiFi or USB
adb connect <watch-ip>:5555     # WiFi
# or plug USB-C cable directly

# 3. Check device
adb devices -l

# 4. Install (use transport_id if multiple devices)
adb -s <serial> uninstall com.army.tectical 2>/dev/null
adb -s <serial> install app/build/outputs/apk/release/app-release.apk

# 5. Select from watch face picker (same long-press → Add flow)
```

---

## Release Signing Setup

### 1. Generate a keystore (one-time)
```bash
keytool -genkeypair -v \
  -keystore tacticalindia-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias tactical \
  -dname "CN=Indian Army Tactical,O=YourCompany"
```

### 2. Configure credentials
Add to `gradle.properties` (keep out of git):
```properties
KEYSTORE_PATH=../tacticalindia-release.jks
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=tactical
KEY_PASSWORD=your_key_password
```

Or set as environment variables:
```bash
export KEYSTORE_PATH=./tacticalindia-release.jks
export KEYSTORE_PASSWORD=secret
export KEY_ALIAS=tactical
export KEY_PASSWORD=secret
```

### 3. Build signed AAB
```bash
./gradlew clean bundleRelease
# AAB is automatically signed + dex-stripped
```

---

## Play Store Deployment

### Pre-submission Checklist

- [ ] `hasCode="false"` in AndroidManifest.xml
- [ ] `isShrinkResources = false` in build.gradle.kts
- [ ] AAB has NO dex files (`unzip -l app-release.aab | grep dex` should be empty)
- [ ] `compileSdk = 35`, `minSdk = 34`, `targetSdk = 35`
- [ ] Preview image is 450x450 PNG in `res/drawable-nodpi/`
- [ ] Release keystore is NOT the debug keystore
- [ ] Package name matches your Play Console app (`com.army.tectical`)
- [ ] `versionCode` is incremented from last published version
- [ ] `android:standalone="true"` meta-data present

### Upload to Play Console

1. Go to [Google Play Console](https://play.google.com/console)
2. Create app → select "Wear OS" as form factor
3. Under **Release** → **Production** → **Create new release**
4. Upload `app/build/outputs/bundle/release/app-release.aab`
5. Set release name (e.g., "1.2.1") and notes

### Required Play Store Assets

| Asset | Size | Notes |
|-------|------|-------|
| App icon | 512x512 PNG | High-res for store listing |
| Feature graphic | 1024x500 PNG | Banner for store page |
| Screenshots | Min 2, 384x384+ | Wear OS round screenshots |
| Short description | Max 80 chars | "Tactical military watch face" |
| Full description | Max 4000 chars | Feature list, customizations |

### Generate Screenshots from Emulator
```bash
# Take screenshot from running Wear OS emulator
adb -s emulator-5556 exec-out screencap -p > screenshot_amber.png

# Resize to 450x450 if needed
sips -z 450 450 screenshot_amber.png --out screenshot_amber_450.png
```

### Content Rating
- Declare "No" for all sensitive categories
- Select "Everyone" rating

### Pricing & Distribution
- WFF watch faces are typically free or paid
- Select "Wear OS" under available devices
- No phone app companion needed (standalone=true)

---

## Design Swapping Workflow

To create a new watch face design from this template:

### 1. Copy the scaffold
```bash
cp -r indian-army-tactical new-watch-face
cd new-watch-face
```

### 2. Update identifiers
- `build.gradle.kts`: change `applicationId` and `namespace`
- `strings.xml`: update `watch_face_name`, labels, flavor names
- `AndroidManifest.xml`: update `android:label` if needed

### 3. Edit the design
All visual design is in `res/raw/watchface.xml`:
- **Colors**: Edit `<ColorConfiguration>` options
- **Layout**: Adjust x/y/width/height of each element
- **Font**: Replace `res/font/dseg7.ttf` with your font, update `family="@font/yourfont"`
- **Complications**: Add/remove `<ComplicationSlot>` elements
- **Flavors**: Update `<Flavors>` section with new preset combos

### 4. Update preview
```bash
# After installing on emulator, capture the rendered watch face:
adb exec-out screencap -p > preview_raw.png
sips -z 450 450 preview_raw.png --out app/src/main/res/drawable-nodpi/preview.png
```

### 5. Build and test
```bash
./gradlew clean assembleRelease
adb uninstall com.your.new.watchface
adb install app/build/outputs/apk/release/app-release.apk
```

---

## Useful WFF Expressions

| Expression | Value |
|------------|-------|
| `[HOUR_0_23_Z]` | 00-23 zero-padded hour |
| `[HOUR_1_12_Z]` | 01-12 zero-padded hour |
| `[MINUTE_Z]` | 00-59 zero-padded minute |
| `[SECOND_Z]` | 00-59 zero-padded second |
| `[DAY_Z]` | 01-31 zero-padded day |
| `[DAY_OF_WEEK_S]` | MON, TUE, etc. |
| `[MONTH_S]` | JAN, FEB, etc. |
| `[YEAR]` | 4-digit year |
| `[DAY_OF_YEAR]` | 1-366 day of year |
| `[BATTERY_PERCENT]` | 0-100 |
| `[HEART_RATE]` | BPM from last reading |
| `[STEP_COUNT]` | Daily step count |
| `[IS_24_HOUR_MODE]` | Boolean: device time format |
| `[CONFIGURATION.id]` | BooleanConfig value (TRUE/FALSE) |
| `[CONFIGURATION.id.N]` | Nth color from ColorConfiguration |
| `[COMPLICATION.TEXT]` | Text value from bound complication |

---

## Failed Approaches Log

Approaches tried and abandoned during development:

| # | Approach | Why It Failed |
|---|----------|---------------|
| 1 | `createUserStyleFlavors()` override in Kotlin | WFF is resource-only (hasCode=false) — can't add Kotlin overrides |
| 2 | `XmlSchemaAndComplicationsDefinition` metadata | Deprecated API, only works with legacy WatchFaceService |
| 3 | `XmlSchemaAndComplicationSlotsDefinition` metadata | Newer name, same issue — requires AndroidX WatchFace runtime |
| 4 | `RUNTIME` intent-filter category | This is an internal category for Google's DWF runtime, not third-party |
| 5 | `FLAVORS_SUPPORTED` metadata in manifest | Was placed in wrong location; needed in `watch_face_info.xml` instead |
| 6 | WFF v1 with FlavorsSupported=true | Flavors are a v2-only feature; v1 generates 0 flavors |
| 7 | XML shape drawable as preview | WFF requires actual PNG bitmap, not vector/shape XML |
| 8 | `letterSpacing` on `<Text>` element | WFF schema requires it on `<Font>`, not `<Text>` |
| 9 | `[DAY_0]` expression | Invalid SourceType; correct is `[DAY_Z]` for zero-padded |
| 10 | `alpha` / `<Variant>` on `<Text>` | WFF ignores alpha on `<Text>`; must use `<PartText>` wrapper |
| 11 | `adb install -r` to update watch face | DWF runtime caches renders; must fully uninstall + reinstall + re-select |
| 12 | Dual `<TimeText>` with alpha toggle in one `<DigitalClock>` | Conflicting alpha/Variant on TimeText caused neither to render |
| 13 | `<Variant>` directly on `<DigitalClock>` | DigitalClock doesn't support Variant as direct child |
| 14 | Placeholder TTF font file | Empty/corrupt TTF silently drops the entire PartText — no error logged |

---

*Generated from Indian Army Digital Tactical v1.2.1 — Watch Face Format v2*
