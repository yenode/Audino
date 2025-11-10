@echo off
REM Audino Application Launcher for Windows
REM This script runs the Audino application

echo.
echo Audino: Intelligent Prescription Manager
echo.

cd /d "%~dp0"

REM Check if project is compiled
if not exist "target\classes" (
    echo Compiling application for first run...
    mvn clean compile
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
mvn javafx:run

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
