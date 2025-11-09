
$ErrorActionPreference = "Stop"

Write-Host "Starting Audino Application..." -ForegroundColor Green

$projectRoot = $PSScriptRoot

Push-Location $projectRoot

try {
    if (-not (Test-Path "pom.xml")) {
        Write-Host "Error: pom.xml not found. Please run setup.ps1 first." -ForegroundColor Red
        exit 1
    }

    Write-Host "Launching application with Maven..." -ForegroundColor Cyan
    
    mvn javafx:run
    
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
