# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this project is

A **Watch Face Format v2 (WFF v2)** Wear OS watch face — entirely resource-based XML with no Kotlin or Java code (`android:hasCode="false"`). The design lives entirely in one file: `indian-army-tactical/app/src/main/res/raw/watchface.xml`.

## Build commands

All commands run from inside the Gradle project root:

```bash
cd indian-army-tactical

# Clean + build APK (sideload) and AAB (Play Store)
./gradlew clean :app:assembleRelease :app:bundleRelease

# Outputs:
#   APK → app/build/outputs/apk/release/app-release.apk
#   AAB → app/build/outputs/bundle/release/app-release.aab
```

Always run `./gradlew clean` after changing `build.gradle.kts` or `AndroidManifest.xml` — Gradle caches aggressively and the APK may silently retain old values.

## Deploy to emulator

```bash
# Uninstall old version first (required — DWF runtime caches renders; -r doesn't clear it)
adb -s emulator-5556 uninstall com.army.tectical 2>/dev/null
adb -s emulator-5556 install app/build/outputs/apk/release/app-release.apk

# Wait ~15s, then verify flavors loaded
sleep 15
adb -s emulator-5556 logcat -d | grep "Finished adding flavors"
# Expect: Success count: 5, Failure count: 0
```

## Architecture

```
indian-army-tactical/          ← Gradle project root
├── app/
│   ├── build.gradle.kts       ← compileSdk=35, minSdk=34, AAB dex-stripping hook
│   └── src/main/
│       ├── AndroidManifest.xml         ← hasCode=false, WFF v2 property
│       └── res/
│           ├── raw/watchface.xml       ← ENTIRE watch face design
│           ├── xml/watch_face_info.xml ← FlavorsSupported, Editable, MultipleInstances
│           ├── font/dseg7.ttf          ← DSEG7 Classic Bold (7-segment LCD font)
│           ├── values/strings.xml      ← all config/flavor labels
│           └── drawable-nodpi/preview.png ← 450×450 picker preview
└── tacticalindia-release.jks  ← release signing keystore (credentials in gradle.properties)

tasks/
├── prompt.md    ← design prompt template + full WFF patterns reference
├── lessons.md   ← pitfalls and lessons learned
└── todo.md      ← backlog and review log
```

### watchface.xml layout (450×450 canvas)

| y range   | Element |
|-----------|---------|
| 30–38     | Indian tricolor accent lines |
| 44–80     | "INDIAN ARMY" header |
| 84–164    | Sub-display: date + weather complication |
| 172–262   | Main time: ghost LCD layer + active DigitalClock |
| 271–275   | Divider double line |
| 283–352   | Bottom panels: BATT / HR / STEPS |
| 360–388   | "=== TACTICAL ===" footer |

### User configurations (UserConfigurations block)

| ID            | Type               | Options |
|---------------|--------------------|---------|
| `accentColor` | ColorConfiguration | amber / green / blue / red / stealth |
| `bgTheme`     | ColorConfiguration | tactical / stealth / urban |
| `showSeconds` | BooleanConfiguration | TRUE / FALSE |

Time format follows the device system setting via `SYNC_TO_DEVICE` / `[IS_24_HOUR_MODE]` — there is no separate config toggle.

Flavors (picker carousel presets) combine `accentColor` + `bgTheme`.

## Critical WFF rules

- **Font references**: always `family="@font/dseg7"` — bare `family="dseg7"` silently drops the entire `<PartText>` with no logcat error.
- **Alpha/Variant**: apply to `<PartText>` / `<PartDraw>`, **not** to `<Text>`, `<DigitalClock>`, or `<TimeText>` directly.
- **Ambient toggle**: `<Variant mode="AMBIENT" target="alpha" value="0"/>` inside a part hides it in ambient; set `alpha="0"` on the part and `value="255"` to show only in ambient.
- **Resource shrinking**: `isShrinkResources = false` is mandatory — the build tool will remove `watchface.xml` otherwise.
- **Dex stripping**: The `afterEvaluate` hook in `build.gradle.kts` forcibly removes `base/dex/*` from the AAB via `zip -d`; this is required for Play Store WFF validation.
- **Reinstalling**: always `adb uninstall` before `adb install` — installing over a different keystore signature fails, and installing over the same signature skips DWF cache invalidation.

## Signing setup

Add to `gradle.properties` (keep out of git):
```properties
KEYSTORE_PATH=../tacticalindia-release.jks
KEYSTORE_PASSWORD=<password>
KEY_ALIAS=tactical
KEY_PASSWORD=<password>
```

Or export as env vars (`KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`). Without these, the build falls back to the debug keystore.

## Workflow

### Planning
- Enter plan mode for any non-trivial task (3+ steps or architectural decisions).
- If something goes sideways, **stop and re-plan immediately** — don't keep pushing.
- Write detailed specs upfront. Use plan mode for verification steps, not just building.
- Write the plan to `tasks/todo.md` with checkable items; check in before starting implementation; mark items complete as you go; add a review section when done.

### Subagents
- Use subagents liberally to keep the main context window clean.
- Offload research, exploration, and parallel analysis to subagents.
- One task per subagent for focused execution.

### Self-improvement
- After any correction from the user: update `tasks/lessons.md` with the pattern.
- Write rules that prevent the same mistake. Review lessons at session start.

### Verification
- Never mark a task complete without proving it works.
- Diff behavior between main and your changes when relevant.
- Run tests, check logs, demonstrate correctness.

### Elegance (balanced)
- For non-trivial changes: pause and ask "is there a more elegant way?"
- If a fix feels hacky: "Knowing everything I know now, implement the elegant solution."
- Skip for simple, obvious fixes — don't over-engineer.

### Bug fixing
- When given a bug report: fix it. Point at logs, errors, failing tests — then resolve them.
- Find root causes. No temporary fixes.

### Core principles
- **Simplicity first**: make every change as simple as possible.
- **Minimal impact**: only touch what's necessary. Avoid introducing unrelated changes.
- **No laziness**: senior developer standards throughout.

## Play Store checklist (before AAB upload)

- `hasCode="false"` in AndroidManifest.xml
- `isShrinkResources = false` in build.gradle.kts
- AAB has no dex: `unzip -l app-release.aab | grep dex` must be empty
- `compileSdk = 35`, `minSdk = 34`, `targetSdk = 35`
- Preview is 450×450 PNG in `res/drawable-nodpi/`
- `versionCode` incremented from last published version
- `android:standalone="true"` meta-data present
