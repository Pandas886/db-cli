$ErrorActionPreference = "Stop"

if (Get-Command dbcli -ErrorAction SilentlyContinue) {
  & dbcli @args
  exit $LASTEXITCODE
}

$SkillDir = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$Bootstrap = Join-Path $SkillDir "scripts/bootstrap_dbcli.ps1"
$BinPath = & $Bootstrap

& $BinPath @args
exit $LASTEXITCODE
