
$ErrorActionPreference = "Stop"

function Resolve-MavenCommand {
    $mvnCommand = Get-Command mvn -ErrorAction SilentlyContinue
    if ($mvnCommand) {
        return $mvnCommand.Source
    }

    $fallback = "C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.11\bin\mvn.cmd"
    if (Test-Path $fallback) {
        return $fallback
    }

    throw "Maven executable not found. Install Maven or add it to PATH."
}

Write-Host "Starting Audino Application..." -ForegroundColor Green

$projectRoot = $PSScriptRoot
$runtimeRoot = Join-Path $env:LOCALAPPDATA "Audino"
$dataDir = Join-Path $runtimeRoot "data"

Push-Location $projectRoot

try {
    $mvnCmd = Resolve-MavenCommand
    $buildDir = Join-Path $env:LOCALAPPDATA "Temp\audino-build"
    New-Item -ItemType Directory -Path $buildDir -Force | Out-Null
    New-Item -ItemType Directory -Path $dataDir -Force | Out-Null

    Write-Host "Verifying target build..." -ForegroundColor Cyan
    if (-not (Test-Path "target/classes")) {
        Write-Host "Compiled classes not found. Building project..." -ForegroundColor Yellow
        & $mvnCmd "-Daudino.build.directory=$buildDir" compile
    }

    Write-Host "Launching application with Maven..." -ForegroundColor Cyan

    & $mvnCmd "-Daudino.build.directory=$buildDir" javafx:run
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: Application failed to start." -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "Error: $_" -ForegroundColor Red
    exit 1
}
finally {
    Pop-Location
}

Write-Host "Application closed." -ForegroundColor Green
