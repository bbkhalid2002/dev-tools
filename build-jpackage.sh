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
jpackage \
  --input target \
  --name "Jasypt UI" \
  --main-jar jasypt-ui-1.0.0-executable.jar \
  --main-class com.jasypt.ui.JasyptGUI \
  --type app-image \
  --app-version 1.0.0 \
  --description "Jasypt AES-GCM Encryption/Decryption Tool" \
  --vendor "Jasypt UI" \
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
