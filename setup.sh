#!/bin/bash

# Audino Setup Script - Automated installation and configuration

echo "🚀 Audino Setup"
echo "==============="

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

print_status() { echo -e "${GREEN}✅ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
print_error() { echo -e "${RED}❌ $1${NC}"; }

# Get script directory and navigate to it
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Check Java
if command -v java >/dev/null 2>&1; then
    JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2)
    print_status "Java found: $JAVA_VERSION"
else
    print_error "Java not found. Install: sudo apt install openjdk-17-jdk"
    exit 1
fi

# Install/Check Maven
if ! command -v mvn >/dev/null 2>&1; then
    print_warning "Installing Maven..."
    sudo apt update && sudo apt install maven -y || {
        print_error "Failed to install Maven"
        exit 1
    }
fi
print_status "Maven available"

# Install OpenJFX
if ! dpkg -l | grep -q openjfx 2>/dev/null; then
    print_warning "Installing OpenJFX..."
    sudo apt install openjfx -y || print_warning "OpenJFX install failed"
fi

# Build application
if [ ! -f "target/audino-1.1.0.jar" ]; then
    echo "Building application..."
    mvn clean package -DskipTests || {
        print_error "Build failed"
        exit 1
    }
fi
print_status "Application built successfully"

# Create simple launcher
cat > start.sh << 'EOF'
#!/bin/bash
echo "🏥 Audino Launcher"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if [ ! -f "target/audino-1.1.0.jar" ]; then
    echo "❌ Application not found. Run: ./setup.sh"
    exit 1
fi

if command -v mvn >/dev/null 2>&1; then
    mvn javafx:run
else
    java -jar target/audino-1.1.0.jar
fi
EOF

chmod +x start.sh
print_status "✅ Setup completed!"
echo ""
echo "Launch Audino:"
echo "  ./audino.sh     # Quick start"
echo "  ./start.sh      # Alternative launcher"
echo "  mvn javafx:run  # Direct Maven command"