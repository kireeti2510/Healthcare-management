#!/bin/bash
# ============================================================
# Healthcare System — Plain Java Build Script (no framework)
# ============================================================
set -e

PROJ_ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$PROJ_ROOT/healthcare"
OUT="$PROJ_ROOT/out"
LIB_H2="$PROJ_ROOT/lib/h2.jar"
CP="$LIB_H2"
MAIN="com.healthcare.Main"

echo "[BUILD] Cleaning output..."
rm -rf "$OUT"
mkdir -p "$OUT"

echo "[BUILD] Compiling Java sources..."
find "$SRC" -name "*.java" > /tmp/sources.txt
javac --release 17 -cp "$CP" -d "$OUT" @/tmp/sources.txt

echo "[BUILD] Compilation successful."
echo ""
echo "[RUN] Starting Healthcare System..."
echo "========================================"
java -cp "$OUT:$CP" "$MAIN"
