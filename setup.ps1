
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

Write-Host "Starting Audino Setup..." -ForegroundColor Green

$projectRoot = $PSScriptRoot

Push-Location $projectRoot

try {
    Write-Host "Checking Java installation..." -ForegroundColor Cyan
    
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: Java is not installed or not in PATH." -ForegroundColor Red
        Write-Host "Please install JDK 11 or higher." -ForegroundColor Yellow
        exit 1
    }
    
    Write-Host "Java found: $javaVersion" -ForegroundColor Green

    Write-Host "Checking Maven installation..." -ForegroundColor Cyan

    $mvnCmd = Resolve-MavenCommand
    $mavenVersion = & $mvnCmd -version 2>&1 | Select-Object -First 1
    $buildDir = Join-Path $env:LOCALAPPDATA "Temp\audino-build"
    New-Item -ItemType Directory -Path $buildDir -Force | Out-Null
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: Maven is not installed or not in PATH." -ForegroundColor Red
        Write-Host "Please install Apache Maven 3.6.0 or higher." -ForegroundColor Yellow
        exit 1
    }
    
    Write-Host "Maven found: $mavenVersion" -ForegroundColor Green

    Write-Host "Cleaning previous builds..." -ForegroundColor Cyan
    & $mvnCmd "-Daudino.build.directory=$buildDir" clean
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: Maven clean failed." -ForegroundColor Red
        exit 1
    }

    Write-Host "Downloading dependencies and compiling..." -ForegroundColor Cyan
    & $mvnCmd "-Daudino.build.directory=$buildDir" compile
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: Maven compile failed." -ForegroundColor Red
        exit 1
    }

    Write-Host "Running tests..." -ForegroundColor Cyan
    & $mvnCmd "-Daudino.build.directory=$buildDir" test
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Warning: Some tests failed. Review output above." -ForegroundColor Yellow
    }

    Write-Host "Verifying data files..." -ForegroundColor Cyan
    
    $dataPath = Join-Path $projectRoot "src\main\resources\data"
    
    $requiredFiles = @(
        "medications.json",
        "patients.json",
        "interaction-rules.json"
    )
    
    foreach ($file in $requiredFiles) {
        $filePath = Join-Path $dataPath $file
        if (-not (Test-Path $filePath)) {
            Write-Host "Warning: $file not found in data directory." -ForegroundColor Yellow
            Write-Host "Creating empty file..." -ForegroundColor Cyan
            New-Item -Path $filePath -ItemType File -Force | Out-Null
            Set-Content -Path $filePath -Value "{}"
        }
    }

    Write-Host "Setup completed successfully!" -ForegroundColor Green
    Write-Host "Run '.\start.ps1' to launch the application." -ForegroundColor Cyan
}
catch {
    Write-Host "Error: $_" -ForegroundColor Red
    exit 1
}
finally {
    Pop-Location
}
