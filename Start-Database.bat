@echo off
echo Compiling Headless Database Runner...
javac -cp "Audino-Executable.jar" StartDatabaseOnly.java
if errorlevel 1 (
    echo Compilation failed! Ensure you have Audino-Executable.jar in the current directory.
    pause
    exit /b 1
)
echo Starting Headless Database Runner...
java -cp "Audino-Executable.jar;." StartDatabaseOnly
