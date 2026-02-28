#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

./scripts/release/build-macos-arm64.sh
./scripts/release/build-linux-amd64.sh

cat <<MSG

Completed local builds.
Produced artifacts:
- dist/releases/dbcli-macos-arm64.tar.gz
- dist/releases/dbcli-linux-amd64.tar.gz

Remaining platforms need matching host/runner:
- macOS x64
- Windows x64
MSG
