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

set "APP_JAR="
for /f "delims=" %%J in ('dir /b /o-d "target\audino-*.jar" 2^>nul') do (
    if not defined APP_JAR set "APP_JAR=%%J"
)

if not defined APP_JAR (
    echo ERROR: No packaged JAR found in target. Expected pattern: audino-*.jar
    pause
    exit /b 1
)

set "JAVAFX_VERSION=19.0.2.1"
set "JAVAFX_ROOT=%USERPROFILE%\.m2\repository\org\openjfx"
set "FX_BASE=%JAVAFX_ROOT%\javafx-base\%JAVAFX_VERSION%\javafx-base-%JAVAFX_VERSION%-win.jar"
set "FX_CONTROLS=%JAVAFX_ROOT%\javafx-controls\%JAVAFX_VERSION%\javafx-controls-%JAVAFX_VERSION%-win.jar"
set "FX_FXML=%JAVAFX_ROOT%\javafx-fxml\%JAVAFX_VERSION%\javafx-fxml-%JAVAFX_VERSION%-win.jar"
set "FX_GRAPHICS=%JAVAFX_ROOT%\javafx-graphics\%JAVAFX_VERSION%\javafx-graphics-%JAVAFX_VERSION%-win.jar"

if not exist "%FX_BASE%" goto :missing_fx
if not exist "%FX_CONTROLS%" goto :missing_fx
if not exist "%FX_FXML%" goto :missing_fx
if not exist "%FX_GRAPHICS%" goto :missing_fx

set "FX_MODULE_PATH=%FX_BASE%;%FX_CONTROLS%;%FX_FXML%;%FX_GRAPHICS%"
set "RUNTIME_ROOT=%LOCALAPPDATA%\AudinoRuntime"
set "RUNTIME_DATA_DIR=%RUNTIME_ROOT%\src\main\resources\data"
if not exist "%RUNTIME_DATA_DIR%" mkdir "%RUNTIME_DATA_DIR%"
set "APP_JAR_PATH=%CD%\target\%APP_JAR%"

echo Starting Audino application from target\%APP_JAR% ...
echo Runtime data directory: %RUNTIME_DATA_DIR%
echo.

REM Run using Maven JavaFX plugin (handles all JavaFX dependencies automatically)
"%MVN_CMD%" -Daudino.build.directory="%BUILD_DIR%" javafx:run

if errorlevel 1 (
    echo.
    echo ERROR: Failed to start Audino application.
    echo Please ensure Java and JavaFX runtime JARs are available.
    echo.
    pause
)

echo.
echo Audino application closed.
pause
exit /b 0

:missing_fx
echo ERROR: JavaFX runtime JARs are missing from local Maven cache.
echo Expected files under:
echo   %JAVAFX_ROOT%
echo Run this once to download dependencies:
echo   mvn dependency:go-offline
echo Then run Audino.bat again.
pause
exit /b 1
