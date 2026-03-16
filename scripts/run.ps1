$ErrorActionPreference = "Stop"

$mavenVersion = "3.9.12"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$mvnCmd = Join-Path $root (".tools\apache-maven-" + $mavenVersion + "\bin\mvn.cmd")

if (!(Test-Path $mvnCmd)) {
  & (Join-Path $PSScriptRoot "setup-maven.ps1")
}

Write-Host "Building..."
& $mvnCmd -q -DskipTests package

$jar = Join-Path $root "target\investment-robot-0.1.0-SNAPSHOT.jar"
if (!(Test-Path $jar)) {
  throw ("Jar not found: " + $jar)
}

Write-Host "Running..."
& java -jar $jar @args

