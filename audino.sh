#!/bin/bash

# Audino Quick Launcher
echo "🏥 Starting Audino..."

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Ensure application is built
[ ! -f "target/audino-1.1.0.jar" ] && {
    echo "Building application..."
    ./setup.sh
}

# Launch using Maven (most reliable for JavaFX)
if command -v mvn >/dev/null 2>&1; then
    echo "� Launching via Maven..."
    mvn javafx:run
else
    echo "🚀 Launching directly..."
    java -jar target/audino-1.1.0.jar
fi