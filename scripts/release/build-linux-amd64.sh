#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

IMAGE="maven:3.9.9-eclipse-temurin-21"

DOCKER_DEFAULT_PLATFORM=linux/amd64 docker run --rm \
  -v "$ROOT:/work" \
  -w /work \
  "$IMAGE" \
  bash -lc './scripts/build-runtime.sh'

OUT_DIR="dist/releases"
PKG_DIR="$OUT_DIR/dbcli-linux-amd64"
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

(cd "$OUT_DIR" && tar -czf dbcli-linux-amd64.tar.gz dbcli-linux-amd64)

echo "Generated: $OUT_DIR/dbcli-linux-amd64.tar.gz"
