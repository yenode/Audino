
$ErrorActionPreference = "Stop"

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
    
    $mavenVersion = mvn -version 2>&1 | Select-Object -First 1
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: Maven is not installed or not in PATH." -ForegroundColor Red
        Write-Host "Please install Apache Maven 3.6.0 or higher." -ForegroundColor Yellow
        exit 1
    }
    
    Write-Host "Maven found: $mavenVersion" -ForegroundColor Green

    Write-Host "Cleaning previous builds..." -ForegroundColor Cyan
    mvn clean
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: Maven clean failed." -ForegroundColor Red
        exit 1
    }

    Write-Host "Downloading dependencies and compiling..." -ForegroundColor Cyan
    mvn compile
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: Maven compile failed." -ForegroundColor Red
        exit 1
    }

    Write-Host "Running tests..." -ForegroundColor Cyan
    mvn test
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Warning: Some tests failed. Review output above." -ForegroundColor Yellow
    }

    Write-Host "Preparing SQLite storage..." -ForegroundColor Cyan

    $sqliteRelativePath = "data/audino.db"
    $configPath = Join-Path $projectRoot "src\main\resources\application.properties"

    if (Test-Path $configPath) {
        $sqlitePathLine = Select-String -Path $configPath -Pattern '^\s*sqlite\.database\.path\s*=\s*(.+)\s*$' | Select-Object -First 1
        if ($sqlitePathLine) {
            $sqliteRelativePath = $sqlitePathLine.Matches[0].Groups[1].Value.Trim()
        }
    }

    $sqlitePath = if ([System.IO.Path]::IsPathRooted($sqliteRelativePath)) {
        $sqliteRelativePath
    } else {
        Join-Path $projectRoot $sqliteRelativePath
    }

    $sqliteDirectory = Split-Path -Parent $sqlitePath
    if ($sqliteDirectory -and -not (Test-Path $sqliteDirectory)) {
        New-Item -Path $sqliteDirectory -ItemType Directory -Force | Out-Null
    }

    if (Test-Path $sqlitePath) {
        Write-Host "SQLite database found at: $sqlitePath" -ForegroundColor Green
    } else {
        Write-Host "SQLite database will be created on first run at: $sqlitePath" -ForegroundColor Cyan
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
