$ErrorActionPreference = "Stop"

$mavenVersion = "3.9.12"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$toolsDir = Join-Path $root ".tools"
$mavenDir = Join-Path $toolsDir ("apache-maven-" + $mavenVersion)
$mvnCmd = Join-Path $mavenDir "bin\mvn.cmd"

if (Test-Path $mvnCmd) {
  Write-Host ("Maven already present: " + $mvnCmd)
  & $mvnCmd -version
  exit 0
}

New-Item -ItemType Directory -Force -Path $toolsDir | Out-Null

$zip = Join-Path $toolsDir ("apache-maven-" + $mavenVersion + "-bin.zip")
$url = "https://dlcdn.apache.org/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"

Write-Host ("Downloading Maven " + $mavenVersion + " ...")
Invoke-WebRequest -Uri $url -OutFile $zip

Write-Host "Extracting..."
Expand-Archive -Path $zip -DestinationPath $toolsDir -Force

if (!(Test-Path $mvnCmd)) {
  throw ("Maven extraction failed. Expected: " + $mvnCmd)
}

Write-Host ("OK: " + $mvnCmd)
& $mvnCmd -version

