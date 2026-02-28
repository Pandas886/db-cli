$ErrorActionPreference = "Stop"

$SkillDir = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$AssetArchive = Join-Path $SkillDir "assets/__BUNDLE_FILE__"
$RuntimeRoot = Join-Path $SkillDir "assets/runtime"
$ExtractedDir = Join-Path $RuntimeRoot "__EXTRACTED_DIR__"
$BinPath = Join-Path $RuntimeRoot "__BIN_REL_PATH__"

if (Test-Path $BinPath) {
  Write-Output $BinPath
  exit 0
}

New-Item -ItemType Directory -Force -Path $RuntimeRoot | Out-Null
if (-not (Test-Path $AssetArchive)) {
  throw "Missing archive: $AssetArchive"
}

if ($AssetArchive.EndsWith('.tar.gz')) {
  tar -xzf $AssetArchive -C $RuntimeRoot
} elseif ($AssetArchive.EndsWith('.zip')) {
  Expand-Archive -Path $AssetArchive -DestinationPath $RuntimeRoot -Force
} else {
  throw "Unsupported archive format: $AssetArchive"
}

if (-not (Test-Path $BinPath)) {
  throw "dbcli binary not found after extraction: $BinPath"
}

Write-Output $BinPath
