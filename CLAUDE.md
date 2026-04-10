# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this project is

A **Watch Face Factory** — a monorepo for mass-producing Wear OS watch faces using Watch Face Format v2 (WFF v2). Each watch face is 100% resource-based XML (`hasCode=false`), generated from a `design.json` spec via a template system. The original "Indian Army Tactical" watch face lives at `indian-army-tactical/`; new designs go in `designs/<slug>/`.

## Watch Face Factory — Creating New Designs

### Quick workflow
```bash
# 1. Create a design spec (or use the /new-watchface skill)
#    Edit designs/<slug>/design.json with colors, fonts, theme

# 2. Generate the project from template
python3 scripts/generate_project.py designs/<slug>/design.json

# 3. Build locally (or push to trigger GitHub Actions)
cd output/<slug>
./gradlew clean :app:assembleRelease :app:bundleRelease
```

### CI/CD — build from anywhere (including Claude mobile app)
Push a `designs/<slug>/design.json` to GitHub → the `build-watchface` workflow auto-triggers → builds AAB+APK → uploads as artifacts. Manual trigger via `workflow_dispatch` also supports publishing to Play Store tracks.

### Skills
- `/new-watchface` — full pipeline: theme → design → generate → build → publish guidance
- `wff-design` — generates design.json from a theme description using WFF design databases
- `wff-generate` — runs template engine, downloads fonts, validates output

## Build commands (original project)

```bash
cd indian-army-tactical
./gradlew clean :app:assembleRelease :app:bundleRelease
```

Always run `./gradlew clean` after changing `build.gradle.kts` or `AndroidManifest.xml` — Gradle caches aggressively.

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
├── template/                              ← frozen scaffold with .tmpl files
│   ├── app/build.gradle.kts.tmpl          ← applicationId, version placeholders
│   ├── app/src/main/res/raw/watchface.xml.tmpl ← colors, fonts, header/footer
│   ├── app/src/main/res/values/strings.xml.tmpl
│   └── (static files: AndroidManifest.xml, gradle wrapper, etc.)
├── designs/                               ← one directory per watch face
│   └── <slug>/
│       ├── design.json                    ← all design tokens (single source of truth)
│       ├── font/<font>.ttf                ← actual font binary
│       └── assets/                        ← preview.png, store screenshots
├── scripts/
│   ├── generate_project.py                ← design.json + template → output/
│   ├── download_font.py                   ← Google Fonts TTF fetcher
│   └── validate_aab.py                    ← verify no dex in AAB
├── data/                                  ← WFF design databases (CSV)
│   ├── wff-color-palettes.csv, wff-fonts.csv, wff-styles.csv
├── output/                                ← GITIGNORED generated projects
├── .claude/skills/                        ← factory skills
│   ├── wff-orchestrator/, wff-design/, wff-generate/
├── .github/workflows/build-watchface.yml  ← CI/CD
├── indian-army-tactical/                  ← original project (kept as-is)
├── website/                               ← landing page + privacy policy
└── tasks/                                 ← prompt.md, lessons.md, todo.md
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
