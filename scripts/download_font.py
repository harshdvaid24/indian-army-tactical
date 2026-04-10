#!/usr/bin/env python3
"""
Download a font from Google Fonts and place it in the design's font directory.

Usage:
    python3 scripts/download_font.py designs/<slug>/design.json
"""

import json
import os
import subprocess
import sys
import tempfile
import zipfile


def download_font(design_path):
    with open(design_path) as f:
        design = json.load(f)

    font = design["font"]
    if font["source"] != "google-fonts":
        print(f"Font source is '{font['source']}', skipping download.")
        return

    design_dir = os.path.dirname(os.path.abspath(design_path))
    font_dir = os.path.join(design_dir, "font")
    os.makedirs(font_dir, exist_ok=True)

    target = os.path.join(font_dir, font["file"])
    if os.path.exists(target):
        print(f"Font already exists: {target}")
        return

    # Derive Google Fonts family name from file slug
    # e.g. "orbitron" -> "Orbitron", "sharetechmono" -> need lookup
    # Use the download URL from wff-fonts.csv if available
    root = os.path.dirname(os.path.dirname(os.path.abspath(design_path)))
    csv_path = os.path.join(root, "data", "wff-fonts.csv")

    download_url = None
    if os.path.exists(csv_path):
        with open(csv_path) as f:
            for line in f:
                parts = line.strip().split(",")
                if len(parts) >= 6 and parts[1].lower().replace(" ", "") == font["family"].lower():
                    download_url = parts[5]
                    break

    if not download_url or download_url == "local":
        # Try constructing URL from family name
        family = font["family"].replace("_", " ").title()
        download_url = f"https://fonts.google.com/download?family={family.replace(' ', '+')}"

    print(f"Downloading font from: {download_url}")

    with tempfile.TemporaryDirectory() as tmpdir:
        zip_path = os.path.join(tmpdir, "font.zip")
        result = subprocess.run(
            ["curl", "-L", "-o", zip_path, download_url],
            capture_output=True, text=True
        )
        if result.returncode != 0:
            print(f"ERROR: Download failed: {result.stderr}")
            sys.exit(1)

        # Check if the download is a valid ZIP or a single TTF
        file_check = subprocess.run(["file", zip_path], capture_output=True, text=True)
        if "TrueType" in file_check.stdout or "OpenType" in file_check.stdout or "font" in file_check.stdout.lower():
            # Direct TTF file (e.g., from GitHub mirror)
            shutil.copy2(zip_path, target)
            print(f"  Direct TTF file saved")
        elif "Zip" in file_check.stdout:
            # ZIP archive from Google Fonts
            try:
                with zipfile.ZipFile(zip_path) as zf:
                    ttf_files = [n for n in zf.namelist() if n.endswith(".ttf")]
                    if not ttf_files:
                        print("ERROR: No TTF files found in downloaded ZIP")
                        sys.exit(1)

                    chosen = None
                    for preference in ["Bold", "Regular", "Medium"]:
                        for f_name in ttf_files:
                            if preference in f_name:
                                chosen = f_name
                                break
                        if chosen:
                            break
                    if not chosen:
                        chosen = ttf_files[0]

                    print(f"  Extracting: {chosen}")
                    with zf.open(chosen) as src, open(target, "wb") as dst:
                        dst.write(src.read())
            except zipfile.BadZipFile:
                print("ERROR: Downloaded file is not a valid ZIP or font")
                sys.exit(1)
        else:
            # Google Fonts blocks CLI; try GitHub mirror
            family_lower = font["family"].lower().replace(" ", "").replace("_", "")
            family_path = font["family"].lower().replace("_", "")
            github_url = f"https://github.com/google/fonts/raw/main/ofl/{family_path}/{font['family'].replace(' ', '')}%5Bwght%5D.ttf"
            print(f"  Primary download blocked, trying GitHub mirror: {github_url}")
            result = subprocess.run(
                ["curl", "-L", "-o", target, github_url],
                capture_output=True, text=True
            )
            if result.returncode != 0:
                print(f"ERROR: GitHub mirror download failed")
                sys.exit(1)
            # Verify it's a real font
            verify = subprocess.run(["file", target], capture_output=True, text=True)
            if "TrueType" not in verify.stdout and "OpenType" not in verify.stdout:
                print(f"ERROR: Downloaded file is not a font: {verify.stdout.strip()}")
                os.remove(target)
                sys.exit(1)

    # Validate
    result = subprocess.run(["file", target], capture_output=True, text=True)
    print(f"  Font type: {result.stdout.strip()}")
    print(f"  Saved to: {target}")


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(f"Usage: {sys.argv[0]} <design.json>")
        sys.exit(1)
    download_font(sys.argv[1])
