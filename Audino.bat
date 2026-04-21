@echo off
REM Audino Application Launcher for Windows
REM This script runs the Audino application

echo.
echo Audino: Intelligent Prescription Manager
echo.

cd /d "%~dp0"

set "BUILD_DIR=%LOCALAPPDATA%\Temp\audino-build"
if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"

set "MVN_CMD=mvn"
where mvn >nul 2>&1
if errorlevel 1 (
    if exist "C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.11\bin\mvn.cmd" (
        set "MVN_CMD=C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.11\bin\mvn.cmd"
    ) else (
        echo ERROR: Maven is not installed or not in PATH.
        echo Install Maven 3.6+ or run setup.ps1.
        pause
        exit /b 1
    )
)

REM Check if project is compiled
if not exist "%BUILD_DIR%\classes" (
    echo Compiling application for first run...
    "%MVN_CMD%" -Daudino.build.directory="%BUILD_DIR%" clean compile
    if errorlevel 1 (
        echo ERROR: Failed to compile application.
        echo Please ensure Maven and Java 17+ are installed.
        pause
        exit /b 1
    )
)

echo Starting Audino application...
echo.

REM Run using Maven JavaFX plugin (handles all JavaFX dependencies automatically)
"%MVN_CMD%" -Daudino.build.directory="%BUILD_DIR%" javafx:run

if errorlevel 1 (
    echo.
    echo ERROR: Failed to start Audino application.
    echo Please ensure Maven and Java 17+ are installed.
    echo.
    pause
)

echo.
echo Audino application closed.
pause
