
$ErrorActionPreference = "Stop"

Write-Host "Starting Audino Application..." -ForegroundColor Green

$projectRoot = $PSScriptRoot

Push-Location $projectRoot

try {
    if (-not (Test-Path "target")) {
        Write-Host "Error: target folder not found. Please build or restore the packaged artifacts first." -ForegroundColor Red
        exit 1
    }

    $jar = Get-ChildItem -Path "target" -Filter "audino-*.jar" -File |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

    if (-not $jar) {
        Write-Host "Error: No packaged JAR found in target (expected audino-*.jar)." -ForegroundColor Red
        exit 1
    }

    $javafxVersion = "19.0.2.1"
    $javafxRoot = Join-Path $env:USERPROFILE ".m2\repository\org\openjfx"
    $javafxJars = @(
        (Join-Path $javafxRoot "javafx-base\$javafxVersion\javafx-base-$javafxVersion-win.jar"),
        (Join-Path $javafxRoot "javafx-controls\$javafxVersion\javafx-controls-$javafxVersion-win.jar"),
        (Join-Path $javafxRoot "javafx-fxml\$javafxVersion\javafx-fxml-$javafxVersion-win.jar"),
        (Join-Path $javafxRoot "javafx-graphics\$javafxVersion\javafx-graphics-$javafxVersion-win.jar")
    )

    $missingJars = $javafxJars | Where-Object { -not (Test-Path $_) }
    if ($missingJars.Count -gt 0) {
        Write-Host "Error: JavaFX runtime JARs are missing from local Maven cache." -ForegroundColor Red
        Write-Host "Missing files:" -ForegroundColor Yellow
        $missingJars | ForEach-Object { Write-Host "  $_" -ForegroundColor Yellow }
        Write-Host "Run 'mvn dependency:go-offline' once, then start again." -ForegroundColor Yellow
        exit 1
    }

    $modulePath = $javafxJars -join ';'
    $runtimeRoot = Join-Path $env:LOCALAPPDATA "AudinoRuntime"
    $runtimeDataDir = Join-Path $runtimeRoot "src\main\resources\data"
    New-Item -ItemType Directory -Path $runtimeDataDir -Force | Out-Null

    Write-Host "Launching $($jar.Name) with JavaFX runtime..." -ForegroundColor Cyan
    Write-Host "Runtime data directory: $runtimeDataDir" -ForegroundColor DarkGray

    $javaArgs = @(
        "-Duser.dir=$runtimeRoot",
        "--module-path", $modulePath,
        "--add-modules", "javafx.controls,javafx.fxml,javafx.graphics",
        "-jar", $jar.FullName
    )

    & java @javaArgs
    
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
