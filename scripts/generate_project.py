#!/usr/bin/env python3
"""
Watch Face Factory — Template Engine

Reads a design.json specification and generates a complete, buildable
Wear OS Watch Face Format v2 Gradle project from the template/ directory.

Usage:
    python3 scripts/generate_project.py designs/<slug>/design.json
"""

import json
import os
import re
import shutil
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEMPLATE_DIR = os.path.join(ROOT, "template")
OUTPUT_DIR = os.path.join(ROOT, "output")


def load_design(path):
    with open(path) as f:
        return json.load(f)


def generate_accent_color_options(design):
    """Generate <ColorOption> XML lines for accentColor."""
    lines = []
    for c in design["accentColors"]:
        colors_str = " ".join(c["colors"])
        lines.append(
            f'            <ColorOption id="{c["id"]}"   displayName="@string/{c["stringKey"]}"\n'
            f'                colors="{colors_str}" />'
        )
    return "\n".join(lines)


def generate_bg_theme_options(design):
    """Generate <ColorOption> XML lines for bgTheme."""
    lines = []
    for t in design["bgThemes"]:
        colors_str = " ".join(t["colors"])
        lines.append(
            f'            <ColorOption id="{t["id"]}" displayName="@string/{t["stringKey"]}"\n'
            f'                colors="{colors_str}" />'
        )
    return "\n".join(lines)


def generate_flavor_options(design):
    """Generate <Flavor> XML blocks."""
    lines = []
    for f in design["flavors"]:
        lines.append(
            f'            <Flavor id="{f["id"]}" displayName="@string/{f["stringKey"]}">\n'
            f'                <Configuration id="accentColor" optionId="{f["accentColor"]}" />\n'
            f'                <Configuration id="bgTheme"     optionId="{f["bgTheme"]}" />\n'
            f'            </Flavor>'
        )
    return "\n".join(lines)


def generate_accent_lines_block(design):
    """Generate tricolor/accent lines XML or empty string."""
    if not design.get("showTricolorAccent", False):
        return ""
    colors = design["tricolorColors"]
    return (
        f'        <PartDraw x="0" y="0" width="450" height="450">\n'
        f'            <Variant mode="AMBIENT" target="alpha" value="0" />\n'
        f'            <Line startX="145" startY="30" endX="305" endY="30"><Stroke color="{colors[0]}" thickness="2" /></Line>\n'
        f'            <Line startX="145" startY="34" endX="305" endY="34"><Stroke color="{colors[1]}" thickness="1" /></Line>\n'
        f'            <Line startX="145" startY="38" endX="305" endY="38"><Stroke color="{colors[2]}" thickness="2" /></Line>\n'
        f'        </PartDraw>'
    )


def generate_accent_color_strings(design):
    """Generate <string> XML lines for accent color labels."""
    lines = []
    for c in design["accentColors"]:
        lines.append(f'    <string name="{c["stringKey"]}">{c["displayName"]}</string>')
    return "\n".join(lines)


def generate_bg_theme_strings(design):
    """Generate <string> XML lines for bg theme labels."""
    lines = []
    for t in design["bgThemes"]:
        lines.append(f'    <string name="{t["stringKey"]}">{t["displayName"]}</string>')
    return "\n".join(lines)


def generate_flavor_strings(design):
    """Generate <string> XML lines for flavor labels."""
    lines = []
    for f in design["flavors"]:
        lines.append(f'    <string name="{f["stringKey"]}">{f["displayName"]}</string>')
    return "\n".join(lines)


def build_replacements(design):
    """Build the full map of {{PLACEHOLDER}} -> value."""
    meta = design["meta"]
    comps = design.get("complicationLabels", {})
    return {
        "DISPLAY_NAME": meta["displayName"],
        "DESCRIPTION": meta["description"],
        "APPLICATION_ID": meta["applicationId"],
        "VERSION_CODE": str(meta["versionCode"]),
        "VERSION_NAME": meta["versionName"],
        "ROOT_PROJECT_NAME": meta.get("rootProjectName", meta["slug"].replace("-", "")),
        "FONT_FAMILY": design["font"]["family"],
        "DEFAULT_ACCENT_COLOR": design["defaultAccentColor"],
        "DEFAULT_BG_THEME": design["defaultBgTheme"],
        "DEFAULT_FLAVOR": design["defaultFlavor"],
        "HEADER_TEXT": design["headerText"],
        "HEADER_COLOR": design.get("headerColor", "#ffE8E8E8"),
        "FOOTER_TEXT": design["footerText"],
        "AMBIENT_SUBTITLE": design.get("ambientSubtitle", design["headerText"]),
        "COMPLICATION_WEATHER_LABEL": comps.get("weather", "Weather / Info"),
        "COMPLICATION_HR_LABEL": comps.get("hr", "Heart Rate"),
        "COMPLICATION_STEPS_LABEL": comps.get("steps", "Step Count"),
        # Block-level replacements (multi-line XML)
        "ACCENT_COLOR_OPTIONS": generate_accent_color_options(design),
        "BG_THEME_OPTIONS": generate_bg_theme_options(design),
        "FLAVOR_OPTIONS": generate_flavor_options(design),
        "ACCENT_LINES_BLOCK": generate_accent_lines_block(design),
        "ACCENT_COLOR_STRINGS": generate_accent_color_strings(design),
        "BG_THEME_STRINGS": generate_bg_theme_strings(design),
        "FLAVOR_STRINGS": generate_flavor_strings(design),
    }


def apply_template(content, replacements):
    """Replace all {{PLACEHOLDER}} occurrences in content."""
    for key, value in replacements.items():
        content = content.replace("{{" + key + "}}", value)
    return content


def check_unresolved(content, filepath):
    """Warn about any remaining {{...}} placeholders."""
    remaining = re.findall(r"\{\{[A-Z_]+\}\}", content)
    if remaining:
        print(f"  WARNING: Unresolved placeholders in {filepath}: {remaining}")
        return False
    return True


def validate_font(font_path):
    """Verify the font is a real TrueType binary."""
    if not os.path.exists(font_path):
        print(f"  ERROR: Font file not found: {font_path}")
        return False
    try:
        result = subprocess.run(["file", font_path], capture_output=True, text=True)
        if "TrueType" not in result.stdout and "OpenType" not in result.stdout and "font" not in result.stdout.lower():
            print(f"  WARNING: Font may not be valid TrueType: {result.stdout.strip()}")
            return False
    except FileNotFoundError:
        pass  # 'file' command not available (e.g., Windows)
    return True


def generate(design_path):
    design = load_design(design_path)
    slug = design["meta"]["slug"]
    design_dir = os.path.dirname(os.path.abspath(design_path))
    out_dir = os.path.join(OUTPUT_DIR, slug)

    print(f"Generating: {slug}")
    print(f"  Template: {TEMPLATE_DIR}")
    print(f"  Output:   {out_dir}")

    # Clean output
    if os.path.exists(out_dir):
        shutil.rmtree(out_dir)

    replacements = build_replacements(design)

    # Walk template directory
    for dirpath, dirnames, filenames in os.walk(TEMPLATE_DIR):
        rel_dir = os.path.relpath(dirpath, TEMPLATE_DIR)
        target_dir = os.path.join(out_dir, rel_dir)
        os.makedirs(target_dir, exist_ok=True)

        for filename in filenames:
            src_path = os.path.join(dirpath, filename)

            if filename.endswith(".tmpl"):
                # Process template
                out_filename = filename[:-5]  # strip .tmpl
                dst_path = os.path.join(target_dir, out_filename)
                with open(src_path) as f:
                    content = f.read()
                content = apply_template(content, replacements)
                check_unresolved(content, dst_path)
                with open(dst_path, "w") as f:
                    f.write(content)
                print(f"  [tmpl] {os.path.relpath(dst_path, out_dir)}")
            else:
                # Copy static file
                dst_path = os.path.join(target_dir, filename)
                shutil.copy2(src_path, dst_path)

    # Copy font
    font_file = design["font"]["file"]
    font_src = os.path.join(design_dir, "font", font_file)
    font_dst_dir = os.path.join(out_dir, "app", "src", "main", "res", "font")
    os.makedirs(font_dst_dir, exist_ok=True)
    font_dst = os.path.join(font_dst_dir, font_file)
    if os.path.exists(font_src):
        shutil.copy2(font_src, font_dst)
        validate_font(font_dst)
        print(f"  [font] {font_file}")
    else:
        print(f"  WARNING: Font not found at {font_src}")

    # Copy preview (required for build — manifest references @drawable/preview)
    preview_dst = os.path.join(out_dir, "app", "src", "main", "res", "drawable-nodpi", "preview.png")
    os.makedirs(os.path.dirname(preview_dst), exist_ok=True)
    preview_src = os.path.join(design_dir, "assets", "preview.png")
    if os.path.exists(preview_src):
        shutil.copy2(preview_src, preview_dst)
        print(f"  [asset] preview.png")
    else:
        # Generate a 1x1 placeholder PNG (will be replaced after emulator capture)
        # Minimal valid PNG: 1x1 black pixel
        import struct, zlib
        def make_placeholder_png():
            sig = b'\x89PNG\r\n\x1a\n'
            ihdr_data = struct.pack('>IIBBBBB', 1, 1, 8, 2, 0, 0, 0)
            ihdr_crc = struct.pack('>I', zlib.crc32(b'IHDR' + ihdr_data) & 0xffffffff)
            ihdr = struct.pack('>I', 13) + b'IHDR' + ihdr_data + ihdr_crc
            raw = zlib.compress(b'\x00\x00\x00\x00')
            idat_crc = struct.pack('>I', zlib.crc32(b'IDAT' + raw) & 0xffffffff)
            idat = struct.pack('>I', len(raw)) + b'IDAT' + raw + idat_crc
            iend_crc = struct.pack('>I', zlib.crc32(b'IEND') & 0xffffffff)
            iend = struct.pack('>I', 0) + b'IEND' + iend_crc
            return sig + ihdr + idat + iend
        with open(preview_dst, 'wb') as f:
            f.write(make_placeholder_png())
        print(f"  [asset] preview.png (placeholder — capture from emulator later)")

    # Ensure gradlew is executable
    gradlew = os.path.join(out_dir, "gradlew")
    if os.path.exists(gradlew):
        os.chmod(gradlew, 0o755)

    print(f"  Done! Project generated at: {out_dir}")
    return out_dir


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(f"Usage: {sys.argv[0]} <design.json>")
        sys.exit(1)
    generate(sys.argv[1])
