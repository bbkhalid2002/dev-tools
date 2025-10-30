#!/bin/bash

# Build Portable Mac Application for Dev Tools Suite
# This script creates a self-contained .app bundle that includes a JRE
# so users don't need Java installed on their Mac

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Building Portable Mac Application${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if running on macOS
if [ "$(uname -s)" != "Darwin" ]; then
    echo -e "${RED}Error: This script must be run on macOS${NC}"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 14 ]; then
    echo -e "${RED}Error: Java 14+ is required for jpackage${NC}"
    echo -e "${YELLOW}Current Java version: $(java -version 2>&1 | head -n 1)${NC}"
    echo ""
    echo "Please install Java 14 or later from:"
    echo "  https://adoptium.net/"
    exit 1
fi

echo -e "${GREEN}✓ Java version check passed${NC}"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Error: Maven is not installed${NC}"
    echo "Please install Maven using: brew install maven"
    exit 1
fi

echo -e "${GREEN}✓ Maven found${NC}"
echo ""

# Clean and build JAR
echo -e "${BLUE}Step 1: Building JAR with Maven...${NC}"
mvn clean package

if [ ! -f "target/dev-tools-suite-1.0.0-executable.jar" ]; then
    echo -e "${RED}Error: JAR file not found after build${NC}"
    exit 1
fi

echo -e "${GREEN}✓ JAR built successfully${NC}"
echo ""

# Create app bundle
echo -e "${BLUE}Step 2: Creating Mac .app bundle...${NC}"
echo -e "${YELLOW}This will bundle a JRE, making the app self-contained${NC}"
echo ""

# Remove old builds
rm -rf target/installer

jpackage \
  --input target \
  --name "Dev Tools Suite" \
  --main-jar dev-tools-suite-1.0.0-executable.jar \
  --main-class com.devtoolssuite.ui.DevToolsSuiteGUI \
  --icon icons/dev_tools_suite.icns \
  --type app-image \
  --app-version 1.0.0 \
  --description "GUI application with developer tools including AES-GCM encryption, JSON viewer, text diff, and more" \
  --vendor "Dev Tools Suite" \
  --dest target/installer \
  --java-options '-Xmx512m'

if [ ! -d "target/installer/Dev Tools Suite.app" ]; then
    echo -e "${RED}Error: .app bundle not created${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Mac .app bundle created successfully${NC}"
echo ""

# Optional: Create DMG for distribution
echo -e "${BLUE}Step 3: Creating DMG installer (optional)...${NC}"
read -p "Do you want to create a .dmg installer? (y/n) " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    DMG_NAME="Dev-Tools-Suite-1.0.0-macOS"

    # Remove old DMG
    rm -f "target/installer/${DMG_NAME}.dmg"

    # Create temporary directory for DMG contents
    DMG_TMP="target/installer/dmg_temp"
    rm -rf "$DMG_TMP"
    mkdir -p "$DMG_TMP"

    # Copy app to temp directory
    cp -R "target/installer/Dev Tools Suite.app" "$DMG_TMP/"

    # Create symlink to Applications folder
    ln -s /Applications "$DMG_TMP/Applications"

    # Create DMG
    hdiutil create -volname "Dev Tools Suite" \
        -srcfolder "$DMG_TMP" \
        -ov -format UDZO \
        "target/installer/${DMG_NAME}.dmg"

    # Clean up temp directory
    rm -rf "$DMG_TMP"

    if [ -f "target/installer/${DMG_NAME}.dmg" ]; then
        echo -e "${GREEN}✓ DMG created successfully${NC}"
        echo ""
        echo -e "${GREEN}DMG location:${NC}"
        echo "  $(pwd)/target/installer/${DMG_NAME}.dmg"
    fi
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Build Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${BLUE}Your portable Mac application is ready:${NC}"
echo -e "  📦 $(pwd)/target/installer/Dev Tools Suite.app"
echo ""
echo -e "${BLUE}File size:${NC}"
du -sh "target/installer/Dev Tools Suite.app"
echo ""
echo -e "${YELLOW}Features:${NC}"
echo "  ✓ Self-contained with bundled JRE"
echo "  ✓ No Java installation required by users"
echo "  ✓ Double-click to run"
echo "  ✓ Native Mac .app bundle"
echo ""
echo -e "${BLUE}To run the application:${NC}"
echo "  1. Double-click 'Dev Tools Suite.app' in Finder"
echo "  2. Or run: open 'target/installer/Dev Tools Suite.app'"
echo ""
echo -e "${BLUE}To distribute:${NC}"
if [ -f "target/installer/${DMG_NAME}.dmg" ]; then
    echo "  • Share the .dmg file (recommended)"
else
    echo "  • Compress the .app: Right-click > Compress"
    echo "  • Share the resulting .zip file"
fi
echo ""
echo -e "${YELLOW}Note: First launch may show a security warning.${NC}"
echo -e "${YELLOW}Users should: Right-click > Open > Open${NC}"
echo ""
