#!/usr/bin/env python3
"""
Validate a WFF v2 AAB has no dex files (required for Play Store).

Usage:
    python3 scripts/validate_aab.py <path-to-aab>
"""

import subprocess
import sys


def validate(aab_path):
    result = subprocess.run(
        ["unzip", "-l", aab_path],
        capture_output=True, text=True
    )
    dex_lines = [l for l in result.stdout.splitlines() if "dex" in l.lower()]

    if dex_lines:
        print(f"FAIL: AAB contains dex files:")
        for line in dex_lines:
            print(f"  {line.strip()}")
        sys.exit(1)
    else:
        print(f"PASS: No dex files in {aab_path}")


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(f"Usage: {sys.argv[0]} <aab-path>")
        sys.exit(1)
    validate(sys.argv[1])
