#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

JAR_VERSION="0.1.0"
APP_VERSION="1.0.0"
APP_NAME="dbcli"
INPUT_DIR="target/package-input"
DIST_DIR="target/dist"

"$ROOT/scripts/build-runtime.sh"

rm -rf "$INPUT_DIR" "$DIST_DIR"
mkdir -p "$INPUT_DIR" "$DIST_DIR"

cp target/db-cli-${JAR_VERSION}.jar "$INPUT_DIR/"
cp -R target/lib "$INPUT_DIR/"

jpackage \
  --type app-image \
  --dest "$DIST_DIR" \
  --name "$APP_NAME" \
  --app-version "$APP_VERSION" \
  --input "$INPUT_DIR" \
  --main-jar "db-cli-${JAR_VERSION}.jar" \
  --main-class com.example.dbcli.Main \
  --runtime-image target/runtime

echo "Packaged app image at: $DIST_DIR/$APP_NAME"
