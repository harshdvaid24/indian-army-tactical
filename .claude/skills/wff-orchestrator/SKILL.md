---
name: wff-orchestrator
description: Create a new watch face from a theme description. Entry point for the watch face factory pipeline.
---

# Watch Face Factory — Orchestrator

You are the entry point for creating new Wear OS watch faces. The user provides a theme and you orchestrate the full pipeline.

## Activation

Triggered by `/new-watchface <theme>` or when the user asks to create a new watch face.

## Workflow

### 1. Gather Requirements

Ask the user for:
- **Theme/concept** (required): e.g. "Cyberpunk Neon", "Luxury Gold", "Ocean Marine"
- **Price** (required): free or paid amount
- **Reference screenshots** (optional): images the user wants the style to match

Also generate these from the theme:
- **Slug**: lowercase-hyphenated (e.g. `cyberpunk-neon`)
- **applicationId**: `com.watchforge.<slug_no_hyphens>` (e.g. `com.watchforge.cyberpunkneon`)
- **Display name**: human-readable (e.g. "Cyberpunk Neon")

### 2. Design Phase — invoke `wff-design` skill

Use the Skill tool to invoke `wff-design` with the theme description.
This produces `designs/<slug>/design.json`.

Present the design to the user:
- Color palette (accent colors + background themes)
- Font choice
- Header/footer text
- Flavor names
- Whether accent lines (tricolor) are included

Wait for user approval. If they want changes, edit design.json directly.

### 3. Generate Phase — invoke `wff-generate` skill

Use the Skill tool to invoke `wff-generate` with the slug.
This runs the template engine and produces a buildable project at `output/<slug>/`.

### 4. Build Phase

The build happens via **GitHub Actions**. After generating design.json:

```bash
git add designs/<slug>/
git commit -m "feat: add <display-name> watch face design"
git push
```

The CI workflow automatically:
- Runs `generate_project.py`
- Builds the AAB
- Uploads the artifact

For manual/local builds:
```bash
cd output/<slug>
./gradlew clean :app:assembleRelease :app:bundleRelease
```

### 5. Play Store Listing

Generate a `STORE_LISTING.md` in `designs/<slug>/assets/` with:
- App name, short description (max 80 chars), full description (max 4000 chars)
- Suggested tags, content rating, category
- Based on the design.json theme and features

### 6. Publishing

- **First upload**: Must be done manually in Google Play Console. Guide the user:
  1. Create app at play.google.com/console
  2. Upload AAB from GitHub Actions artifacts
  3. Fill listing from STORE_LISTING.md
  4. Set pricing
- **Subsequent updates**: Trigger the `build-watchface` workflow with `track: production`

## Important Rules

- Every watch face is a SEPARATE Android app with a unique applicationId
- Font files MUST be real TTF binaries — never use placeholders
- Always `./gradlew clean` after template changes
- The existing `com.army.tectical` ID is grandfathered; new faces use `com.watchforge.*`
