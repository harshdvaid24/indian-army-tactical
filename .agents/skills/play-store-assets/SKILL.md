---
name: play-store-assets
description: >
  Generates Play Store assets for watch face + companion app pairs. Captures screenshots from
  emulators, generates app icons, feature graphics, and STORE_LISTING.md. Handles both Wear OS
  watch faces and phone companion apps. Use when user says "prepare play store assets",
  "store listing", "play store screenshots", "/play-store", or "prepare for publishing".
  Requires watch + phone emulators running with apps installed.
applyTo: "**"
---

# Play Store Assets Skill

Generate all Google Play Store assets for a watch face + companion app pair.

## Prerequisites

- Watch emulator running with watch face installed and set as active face
- Phone emulator running with companion app installed
- Both apps built (debug or release)
- `adb`, `sips` (macOS), `convert` (ImageMagick, optional) available

## Identify Emulators

```bash
adb devices -l
```
- Watch: `sdk_gwear_arm64` → typically emulator-5556
- Phone: `sdk_gphone64_arm64` → typically emulator-5554

## Asset Specifications

| Asset | Dimensions | Format | Required |
|-------|-----------|--------|----------|
| App Icon | 512×512 | PNG, 32-bit, no alpha | Yes |
| Feature Graphic | 1024×500 | PNG or JPG | Yes |
| Phone Screenshots | 1080×1920+ (portrait) | PNG | Min 2, max 8 |
| Watch Screenshots | 384×384 or native | PNG | Min 2, max 8 (Wear OS) |
| Short Description | max 80 chars | text | Yes |
| Full Description | max 4000 chars | text | Yes |

## Step-by-Step Workflow

### 1. Capture Watch Face Screenshots

```bash
WATCH=emulator-5556
OUTDIR=play_store_assets/<app-name>

# Go to watch face home
adb -s $WATCH shell input keyevent KEYCODE_HOME
sleep 2

# Capture main watch face (active mode)
adb -s $WATCH exec-out screencap -p > $OUTDIR/watch_screenshot_1.png

# For ambient mode: briefly turn off screen, then capture
adb -s $WATCH shell input keyevent KEYCODE_POWER
sleep 2
adb -s $WATCH shell input keyevent KEYCODE_POWER
sleep 1
adb -s $WATCH exec-out screencap -p > $OUTDIR/watch_screenshot_ambient.png
```

To switch watch faces (for multi-face screenshots):
```bash
# Long-press center to open picker
adb -s $WATCH shell input swipe 225 225 225 225 2000
sleep 3
# Navigate with left/right arrows or swipe
adb -s $WATCH shell input tap 305 410   # right arrow
adb -s $WATCH shell input tap 155 410   # left arrow
# Tap center to select
adb -s $WATCH shell input tap 225 225
```

### 2. Capture Phone Companion Screenshots

```bash
PHONE=emulator-5554

# Launch companion app
adb -s $PHONE shell am start -n <package>/.MainActivity
sleep 3

# Capture full screen
adb -s $PHONE exec-out screencap -p > $OUTDIR/phone_screenshot_1.png

# Scroll down for more content
adb -s $PHONE shell input swipe 540 1500 540 500 500
sleep 1
adb -s $PHONE exec-out screencap -p > $OUTDIR/phone_screenshot_2.png
```

### 3. Generate App Icon (512×512)

The app icon should be the watch face screenshot — same icon for both watch face and companion app.

```bash
# Crop watch screenshot to square content area, then resize to 512x512
# For round watch faces, the emulator captures 450x450 with round mask
cp $OUTDIR/watch_screenshot_1.png $OUTDIR/app_icon_raw.png

# Resize to exactly 512x512 (macOS sips)
sips -z 512 512 $OUTDIR/app_icon_raw.png --out $OUTDIR/app_icon_512.png

# Verify
sips -g pixelWidth -g pixelHeight $OUTDIR/app_icon_512.png
```

If ImageMagick available (better quality, adds rounded corners):
```bash
convert $OUTDIR/watch_screenshot_1.png \
  -resize 512x512 \
  \( +clone -alpha extract -draw 'fill black polygon 0,0 0,64 64,0 fill white circle 64,64 64,0' \
  \( +clone -flip \) -compose Multiply -composite \
  \( +clone -flop \) -compose Multiply -composite \) \
  -alpha off -compose CopyOpacity -composite \
  $OUTDIR/app_icon_512.png
```

### 4. Generate Feature Graphic (1024×500)

Feature graphic = banner image shown at top of Play Store listing.

```bash
# Create dark background with watch face centered
# Option A: Simple — dark solid + watch face centered
convert -size 1024x500 xc:'#0A0A0A' \
  $OUTDIR/watch_screenshot_1.png -gravity center -composite \
  -font Courier-Bold -pointsize 48 -fill '#FF8C00' \
  -gravity south -annotate +0+30 'APP NAME' \
  $OUTDIR/feature_graphic_1024x500.png

# Option B: Without ImageMagick — just use a pre-designed template
# or use the watch screenshot scaled to fit
sips -z 500 500 $OUTDIR/watch_screenshot_1.png --out /tmp/fg_watch.png
# Pad to 1024x500 (requires ImageMagick or manual composition)
```

### 5. Set App Icons in Both Projects

Copy the 512×512 icon into both projects as the launcher icon:

```bash
# Watch face project
cp $OUTDIR/app_icon_512.png <watch-project>/app/src/main/res/drawable-nodpi/preview.png

# Companion app project — multiple sizes for adaptive icon
# For simplicity, use single drawable-nodpi:
cp $OUTDIR/app_icon_512.png <companion-project>/app/src/main/res/drawable-nodpi/wf_icon.png
cp $OUTDIR/app_icon_512.png <companion-project>/app/src/main/res/drawable-nodpi/wf_preview.png
```

### 6. Generate STORE_LISTING.md

Create `play_store_assets/<app-name>/STORE_LISTING.md` with:

```markdown
# Google Play Store Listing — <App Name>

## App Name
<name> (max 30 chars)

## Short Description (max 80 chars)
<one-liner describing the watch face>

## Full Description (max 4000 chars)
<structured description with FEATURES, CUSTOMIZATION, DESIGN, TECHNICAL sections>

## Content Rating
Everyone

## Category
Watch Face (for watch face) / Personalization (for companion)

## Tags
watch face, <style>, digital, LCD, Wear OS, complications, customizable

## Asset Checklist
- [ ] app_icon_512.png (512×512)
- [ ] feature_graphic_1024x500.png (1024×500)
- [ ] watch_screenshot_1.png (main face)
- [ ] watch_screenshot_2.png (ambient or alternate theme)
- [ ] phone_screenshot_1.png (companion hero)
- [ ] phone_screenshot_2.png (companion scrolled)

## Upload Steps
1. Play Console → Select app → Store presence → Main store listing
2. Upload app icon, feature graphic, screenshots
3. Fill short + full description
4. Set content rating → Everyone
5. Release → Production → upload .aab
```

### 7. Rebuild with Final Icons

```bash
cd <watch-project>
./gradlew clean :app:assembleRelease :app:bundleRelease

cd <companion-project>
./gradlew clean :app:assembleDebug  # or assembleRelease if signing configured
```

### 8. Verify AAB (Watch Face)

```bash
# Must have no dex files
unzip -l app/build/outputs/bundle/release/app-release.aab | grep dex
# Should return empty

# Check icon present
unzip -l app/build/outputs/bundle/release/app-release.aab | grep preview
```

## Quality Checklist

- [ ] App icon is 512×512, no transparency, recognizable at small size
- [ ] Feature graphic is 1024×500, not cropped awkwardly
- [ ] Min 2 screenshots per device type (watch, phone)
- [ ] Screenshots show actual app running (not mockups)
- [ ] Short description ≤ 80 chars
- [ ] Full description ≤ 4000 chars, no spam keywords
- [ ] Both watch + companion use same app icon
- [ ] versionCode incremented from last published version
- [ ] AAB has no dex (watch face only)
- [ ] Content rating set to Everyone

## Common Pitfalls

- **Round watch mask**: Emulator screenshots include black corners. Play Store accepts this for Wear OS listings.
- **sips vs ImageMagick**: macOS `sips` can resize but can't add padding or compose. Use ImageMagick `convert` for feature graphics.
- **Icon too dark**: Watch faces are often dark-themed. Ensure icon has enough contrast to be recognizable in Play Store listing (light app drawer backgrounds).
- **Companion app icon**: Must match watch face icon for brand consistency.
- **Screenshot dimensions**: Phone screenshots must be 16:9 or taller. Watch screenshots should be square.
