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

        # Extract TTF files
        try:
            with zipfile.ZipFile(zip_path) as zf:
                ttf_files = [n for n in zf.namelist() if n.endswith(".ttf")]
                if not ttf_files:
                    print("ERROR: No TTF files found in downloaded ZIP")
                    sys.exit(1)

                # Prefer Bold or Regular weight
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
            print("ERROR: Downloaded file is not a valid ZIP")
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
