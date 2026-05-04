
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
$sqlitePath = Join-Path $dataDir "audino.db"
$projectSqlitePath = Join-Path $projectRoot "data\audino.db"
$sqliteBackupPath = Join-Path $projectRoot "data\audino.db.bak"
$etlSqlitePath = Join-Path $projectRoot "..\audino-etl\artifacts\latest\audino_master.db"

Push-Location $projectRoot

try {
    if (-not (Test-Path "target")) {
        Write-Host "Error: target folder not found. Please build or restore the packaged artifacts first." -ForegroundColor Red
        exit 1
    }

    $mvnCmd = Resolve-MavenCommand
    $buildDir = Join-Path $env:LOCALAPPDATA "Temp\audino-build"
    New-Item -ItemType Directory -Path $buildDir -Force | Out-Null
    New-Item -ItemType Directory -Path $dataDir -Force | Out-Null

    if (-not (Test-Path $sqlitePath)) {
        if (Test-Path $projectSqlitePath) {
            Copy-Item -Path $projectSqlitePath -Destination $sqlitePath -Force
            Write-Host "Seeded runtime SQLite database from project copy: $projectSqlitePath" -ForegroundColor Yellow
        }
        elseif (Test-Path $sqliteBackupPath) {
            Copy-Item -Path $sqliteBackupPath -Destination $sqlitePath -Force
            Write-Host "Restored SQLite database from backup: $sqliteBackupPath" -ForegroundColor Yellow
        }
        elseif (Test-Path $etlSqlitePath) {
            Copy-Item -Path $etlSqlitePath -Destination $sqlitePath -Force
            Write-Host "Seeded SQLite database from ETL artifact: $etlSqlitePath" -ForegroundColor Yellow
        }
        else {
            Write-Host "SQLite database file not found. A new database will be created at: $sqlitePath" -ForegroundColor Yellow
        }
    }

    Write-Host "Using runtime SQLite database: $sqlitePath" -ForegroundColor Cyan

    Write-Host "Launching application with Maven..." -ForegroundColor Cyan

    & $mvnCmd "-Daudino.build.directory=$buildDir" "-Daudino.sqlite.path=$sqlitePath" javafx:run
    
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
