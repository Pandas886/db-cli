#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

export JAVA_HOME="${JAVA_HOME:-/Users/huzekang/Library/Java/JavaVirtualMachines/corretto-21.0.6/Contents/Home}"
export PATH="$JAVA_HOME/bin:$PATH"

scripts/build-runtime.sh

OUT_DIR="dist/releases"
PKG_DIR="$OUT_DIR/dbcli-macos-arm64"
rm -rf "$PKG_DIR"
mkdir -p "$PKG_DIR/bin" "$PKG_DIR/app"

cp target/db-cli-0.1.0.jar "$PKG_DIR/app/"
cp -R target/lib "$PKG_DIR/app/"
if [ -d "$ROOT/drivers" ]; then
  find "$ROOT/drivers" -maxdepth 1 -type f -name '*.jar' -exec cp {} "$PKG_DIR/app/lib/" \;
fi
cp -R target/runtime "$PKG_DIR/runtime"
cp README.md "$PKG_DIR/README.md"
cp examples/datasources.json "$PKG_DIR/datasources.example.json"

cat > "$PKG_DIR/bin/dbcli" <<'LAUNCHER'
#!/usr/bin/env bash
set -euo pipefail
SELF_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SELF_DIR/.." && pwd)"
exec "$ROOT_DIR/runtime/bin/java" -cp "$ROOT_DIR/app/db-cli-0.1.0.jar:$ROOT_DIR/app/lib/*" com.example.dbcli.Main "$@"
LAUNCHER

chmod +x "$PKG_DIR/bin/dbcli"

(cd "$OUT_DIR" && tar -czf dbcli-macos-arm64.tar.gz dbcli-macos-arm64)

echo "Generated: $OUT_DIR/dbcli-macos-arm64.tar.gz"
