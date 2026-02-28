#!/usr/bin/env bash
set -euo pipefail

SKILL_DIR="$(cd "$(dirname "$0")/.." && pwd)"
ASSET_ARCHIVE="$SKILL_DIR/assets/__BUNDLE_FILE__"
RUNTIME_ROOT="$SKILL_DIR/assets/runtime"
EXTRACTED_DIR="$RUNTIME_ROOT/__EXTRACTED_DIR__"
BIN_PATH="$RUNTIME_ROOT/__BIN_REL_PATH__"

if [ -x "$BIN_PATH" ] || [ -f "$BIN_PATH" ]; then
  echo "$BIN_PATH"
  exit 0
fi

mkdir -p "$RUNTIME_ROOT"
if [ ! -f "$ASSET_ARCHIVE" ]; then
  echo "Missing archive: $ASSET_ARCHIVE" >&2
  exit 1
fi

case "$ASSET_ARCHIVE" in
  *.tar.gz)
    tar -xzf "$ASSET_ARCHIVE" -C "$RUNTIME_ROOT"
    ;;
  *.zip)
    unzip -o "$ASSET_ARCHIVE" -d "$RUNTIME_ROOT" >/dev/null
    ;;
  *)
    echo "Unsupported archive format: $ASSET_ARCHIVE" >&2
    exit 1
    ;;
esac

if [ ! -x "$BIN_PATH" ] && [ ! -f "$BIN_PATH" ]; then
  echo "dbcli binary not found after extraction: $BIN_PATH" >&2
  exit 1
fi

echo "$BIN_PATH"
