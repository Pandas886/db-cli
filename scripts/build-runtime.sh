#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if command -v mvnd >/dev/null 2>&1; then
  BUILD_CMD="mvnd"
elif command -v mvn >/dev/null 2>&1; then
  BUILD_CMD="mvn"
else
  echo "Neither mvnd nor mvn found in PATH" >&2
  exit 1
fi

"$BUILD_CMD" -q -DskipTests package

APP_JAR="target/db-cli-0.1.0.jar"
LIB_DIR="target/lib"
RUNTIME_DIR="target/runtime"
MODULES="java.base,java.sql,java.logging,java.desktop,java.management,java.naming,java.security.sasl,jdk.unsupported"

rm -rf "$RUNTIME_DIR"

jlink \
  --add-modules "$MODULES" \
  --strip-debug \
  --no-header-files \
  --no-man-pages \
  --output "$RUNTIME_DIR"

echo "Runtime image generated at: $RUNTIME_DIR"
echo "Run with: $RUNTIME_DIR/bin/java -cp $APP_JAR:$LIB_DIR/* com.example.dbcli.Main"
