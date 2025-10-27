#!/bin/bash

echo "========================================"
echo "Building Native Installer with jpackage"
echo "========================================"
echo ""
echo "Note: This requires Java 14+ with jpackage"
echo ""

echo "Cleaning and building JAR..."
mvn clean package

echo ""
echo "Creating native installer..."

# Choose icon per platform: macOS (.icns), Linux (.png)
OS_NAME=$(uname -s)
ICON_OPT="--icon icons/dev_tools_suite.png"
if [ "$OS_NAME" = "Darwin" ]; then
  ICON_OPT="--icon icons/dev_tools_suite.icns"
fi

jpackage \
  --input target \
  --name "Dev Tools Suite" \
  --main-jar dev-tools-suite-1.0.0-executable.jar \
  --main-class com.devtoolssuite.ui.DevToolsSuiteGUI \
  $ICON_OPT \
  --type app-image \
  --app-version 1.0.0 \
  --description "AES-GCM Encryption/Decryption Tool" \
  --vendor "Dev Tools Suite" \
  --dest target/installer

echo ""
echo "========================================"
echo "Build Complete!"
echo "========================================"
echo ""
echo "Application created in: target/installer/"
echo ""
echo "This includes a bundled JRE,"
echo "so users don't need Java installed!"
echo ""
