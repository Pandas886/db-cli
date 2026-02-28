#!/usr/bin/env bash
set -euo pipefail

SKILL_DIR="$(cd "$(dirname "$0")/.." && pwd)"
BOOTSTRAP="$SKILL_DIR/scripts/bootstrap_dbcli.sh"

if command -v dbcli >/dev/null 2>&1; then
  exec dbcli "$@"
fi

BIN_PATH="$($BOOTSTRAP)"
if [[ "$BIN_PATH" == *.cmd ]]; then
  exec cmd.exe /c "$BIN_PATH" "$@"
fi
exec "$BIN_PATH" "$@"
