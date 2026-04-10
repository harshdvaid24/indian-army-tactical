---
name: wff-design
description: Design a watch face theme — generates design.json from a theme description using WFF design databases.
---

# Watch Face Design Intelligence

Given a theme description, produce a complete `design.json` specification for a WFF v2 watch face.

## Process

### 1. Search Design Databases

Read and search these CSV files in `data/`:

- `wff-color-palettes.csv` — Find the best matching color palette by keywords
- `wff-fonts.csv` — Find the best matching font by keywords
- `wff-styles.csv` — Find the style archetype for header/footer format

Use Grep to search by keywords from the user's theme description.

### 2. If UI/UX Pro Max is installed

Also run the search engine for deeper design intelligence:
```bash
python3 .claude/skills/ui-ux-pro-max/scripts/search.py "<theme>" --design-system
```

Use the results to inform color choices, but always map to the WFF 3-tuple format:
- accent: `[primary_lcd, secondary_dim, frame_dark]`
- bg: `[scene_near_black, panel_slightly_lighter, stroke_accent_tinted]`

### 3. Compose design.json

Build the full specification:

```json
{
  "meta": {
    "slug": "<slug>",
    "displayName": "<Display Name>",
    "description": "<Play Store description>",
    "applicationId": "com.watchforge.<slugnohyphens>",
    "versionCode": 1,
    "versionName": "1.0.0",
    "rootProjectName": "<PascalCaseName>"
  },
  "font": {
    "family": "<font_file_slug>",
    "file": "<font_file_slug>.ttf",
    "source": "google-fonts" or "local"
  },
  "accentColors": [
    { "id": "...", "displayName": "...", "stringKey": "color_...", "colors": ["primary", "secondary", "frame"] }
    // 3-5 options
  ],
  "defaultAccentColor": "<first_accent_id>",
  "bgThemes": [
    { "id": "...", "displayName": "...", "stringKey": "bg_...", "colors": ["bg", "panel", "stroke"] }
    // 2-3 themes
  ],
  "defaultBgTheme": "<first_bg_id>",
  "flavors": [
    { "id": "...", "displayName": "...", "stringKey": "flavor_...", "accentColor": "<accent_id>", "bgTheme": "<bg_id>" }
    // 3-5 presets combining accent + bg
  ],
  "defaultFlavor": "<first_flavor_id>",
  "headerText": "<HEADER>",
  "headerColor": "#ffE8E8E8",
  "footerText": "<formatted footer>",
  "ambientSubtitle": "<same as header or shorter>",
  "showTricolorAccent": false,
  "tricolorColors": [],
  "complicationLabels": {
    "weather": "Weather / Info",
    "hr": "Heart Rate",
    "steps": "Step Count"
  },
  "playStore": {
    "shortDescription": "<max 80 chars>",
    "price": "<free or amount>",
    "category": "Watch Face",
    "contentRating": "Everyone"
  }
}
```

### 4. Color Rules

**Accent colors** (the LCD digits and UI elements):
- `.0` (primary): bright, vivid — this is the main time display color
- `.1` (secondary): slightly muted — used for date text and labels
- `.2` (frame): dark, muted — used for bezel rings, panel borders, dim labels

**Background themes** (the canvas and panels):
- `.0` (scene): near-black — watch faces need dark backgrounds for OLED
- `.1` (panel): slightly lighter than scene — fills info panels
- `.2` (stroke): tinted with accent hue at low opacity — panel borders

**Rules:**
- Scene background must be very dark (value < 20 in HSV)
- Panel background must be darker than any accent color
- Frame color must be darker than secondary accent
- All colors use `#ffRRGGBB` format (WFF requires alpha prefix)

### 5. Font Selection

Match the theme mood to font categories:
- Military/tactical → `digital` (dseg7)
- Cyberpunk/futuristic → `display` (orbitron, audiowide, oxanium)
- Minimalist/clean → `sans-serif` (play, exo2, rajdhani)
- Sport/racing → `sans-serif` or `display` (saira, teko, brunoace)
- Luxury/elegant → `display` (michroma)

If font source is `google-fonts`, the generate phase will download it.
If `local`, it must already exist in `designs/<slug>/font/`.

### 6. Header/Footer Formatting

Based on the style archetype's `header_format` and `footer_format`:
- `UPPER`: all caps, e.g. "CYBERPUNK"
- `TITLE`: title case, e.g. "Cyberpunk"
- Footer wraps: `=== LABEL ===`, `// LABEL //`, `- LABEL -`, etc.

### 7. Tricolor Accent Lines

Only set `showTricolorAccent: true` for Indian patriotic/army themes.
The tricolor colors are always: `["#ffFF9933", "#ffE8E8E8", "#ff138808"]` (saffron, white, green).

### 8. Write Output

Write the design.json to `designs/<slug>/design.json`.
Create the `designs/<slug>/font/` and `designs/<slug>/assets/` directories.

Report the design summary to the orchestrator/user for approval.
