#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -ne 4 ]; then
  echo "Usage: $0 <platform-label> <binary-archive-path> <archive-root-dir> <output-zip-path>" >&2
  exit 1
fi

PLATFORM_LABEL="$1"
BINARY_ARCHIVE="$2"
ARCHIVE_ROOT_DIR="$3"
OUTPUT_ZIP="$4"

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
TEMPLATE_DIR="$ROOT/skill-template/db-cli"
WORK_DIR="$ROOT/target/skill-package/$PLATFORM_LABEL"
SKILL_DIR="$WORK_DIR/db-cli"

if [ ! -f "$BINARY_ARCHIVE" ]; then
  echo "Binary archive not found: $BINARY_ARCHIVE" >&2
  exit 1
fi

rm -rf "$WORK_DIR"
mkdir -p "$SKILL_DIR"
cp -R "$TEMPLATE_DIR"/* "$SKILL_DIR/"

case "$BINARY_ARCHIVE" in
  *.tar.gz)
    BUNDLE_FILE="dbcli-bundle.tar.gz"
    ;;
  *.zip)
    BUNDLE_FILE="dbcli-bundle.zip"
    ;;
  *)
    echo "Unsupported binary archive extension: $BINARY_ARCHIVE" >&2
    exit 1
    ;;
esac

BIN_REL_PATH="$ARCHIVE_ROOT_DIR/bin/dbcli"
if [[ "$PLATFORM_LABEL" == windows-* ]]; then
  BIN_REL_PATH="$ARCHIVE_ROOT_DIR/bin/dbcli.exe"
fi

mkdir -p "$SKILL_DIR/assets"
cp "$BINARY_ARCHIVE" "$SKILL_DIR/assets/$BUNDLE_FILE"

for target in "$SKILL_DIR/SKILL.md" "$SKILL_DIR/scripts/bootstrap_dbcli.sh" "$SKILL_DIR/scripts/bootstrap_dbcli.ps1"; do
  tmp_file="$target.tmp"
  sed \
    -e "s|__BUNDLE_FILE__|$BUNDLE_FILE|g" \
    -e "s|__EXTRACTED_DIR__|$ARCHIVE_ROOT_DIR|g" \
    -e "s|__BIN_REL_PATH__|$BIN_REL_PATH|g" \
    "$target" > "$tmp_file"
  mv "$tmp_file" "$target"
done

chmod +x "$SKILL_DIR/scripts/bootstrap_dbcli.sh" "$SKILL_DIR/scripts/run_dbcli.sh"

mkdir -p "$(dirname "$OUTPUT_ZIP")"
rm -f "$OUTPUT_ZIP"
if command -v zip >/dev/null 2>&1; then
  (
    cd "$WORK_DIR"
    zip -r "$OUTPUT_ZIP" db-cli >/dev/null
  )
elif command -v powershell.exe >/dev/null 2>&1; then
  if command -v cygpath >/dev/null 2>&1; then
    ps_src="$(cygpath -w "$WORK_DIR/db-cli")"
    ps_dst="$(cygpath -w "$OUTPUT_ZIP")"
  else
    ps_src="$WORK_DIR/db-cli"
    ps_dst="$OUTPUT_ZIP"
  fi
  powershell.exe -NoProfile -Command "Compress-Archive -Path '$ps_src' -DestinationPath '$ps_dst' -Force" >/dev/null
else
  echo "No archive tool available (zip or powershell.exe)" >&2
  exit 1
fi

echo "Generated skill zip: $OUTPUT_ZIP"
