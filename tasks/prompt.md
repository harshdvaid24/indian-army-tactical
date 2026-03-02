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
app/
├── src/main/
│   ├── AndroidManifest.xml          ← CRITICAL: must include WatchFaceStubService
│   ├── res/
│   │   ├── raw/watchface.xml        ← entire watch face design lives here
│   │   ├── font/dseg7.ttf           ← DSEG7 Classic Bold v0.46 binary
│   │   ├── values/strings.xml       ← all configuration labels
│   │   └── drawable/preview.png     ← 450×450 picker preview
│   └── (no Kotlin source needed)
├── build.gradle.kts                 ← compileSdk=35, minSdk=30, isShrinkResources=false
tasks/
├── prompt.md                        ← this file
├── todo.md
└── lessons.md
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
# Full clean build
cd /path/to/project
./gradlew clean :app:assembleRelease :app:bundleRelease

# Install on emulator
APK=app/build/outputs/apk/release/app-release.apk
adb -s emulator-5556 uninstall com.yourapp.id
adb -s emulator-5556 install $APK

# Install on physical device (find transport_id with: adb devices -l)
adb -t 27 uninstall com.yourapp.id
adb -t 27 install $APK

# Verify service registered (confirms picker will find it)
adb shell dumpsys package com.yourapp.id | grep -A2 "WATCH_FACE"
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
| `[BATTERY_PERCENT]` | 0-100 |
| `[CONFIGURATION.id]` | Integer index of a ListConfiguration option |
| `[CONFIGURATION.id.N]` | Nth color from a ColorConfiguration option |
| `[COMPLICATION.TEXT]` | Text value from bound complication |

---

*Generated from Indian Army Digital Tactical v1.0.0 — Watch Face Format v2 template*
