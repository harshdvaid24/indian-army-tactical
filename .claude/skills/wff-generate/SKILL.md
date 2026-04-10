---
name: wff-generate
description: Generate a buildable WFF v2 project from a design.json using the template engine.
---

# Watch Face Generator

Takes a `design.json` and produces a complete, buildable Gradle project.

## Process

### 1. Validate design.json

Check that `designs/<slug>/design.json` exists and has all required fields:
- `meta.slug`, `meta.displayName`, `meta.applicationId`, `meta.versionCode`, `meta.versionName`
- `font.family`, `font.file`, `font.source`
- `accentColors` (at least 3 options with 3 colors each)
- `bgThemes` (at least 2 options with 3 colors each)
- `flavors` (at least 3 entries)
- `headerText`, `footerText`

### 2. Download Font (if needed)

If `font.source` is `"google-fonts"`:
```bash
python3 scripts/download_font.py designs/<slug>/design.json
```

This downloads the TTF from Google Fonts and places it in `designs/<slug>/font/`.

If `font.source` is `"local"`, verify the font file exists at `designs/<slug>/font/<file>`.

### 3. Run Template Engine

```bash
python3 scripts/generate_project.py designs/<slug>/design.json
```

This generates the full project at `output/<slug>/`.

### 4. Validate Output

Check:
- No unresolved `{{PLACEHOLDER}}` in any output file
- Font file is a real TrueType binary (not HTML or empty)
- `watchface.xml` has the correct number of ColorOptions and Flavors
- `build.gradle.kts` has the correct applicationId

### 5. Report

Tell the user:
- Project generated at `output/<slug>/`
- Font: `<family>` from `<source>`
- Accent colors: N options
- Flavors: N presets
- Ready to build (locally or via CI)
