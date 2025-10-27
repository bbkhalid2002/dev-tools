#!/bin/bash

echo "========================================"
echo "Building Dev Tools Suite Executable"
echo "========================================"
echo ""

echo "Cleaning previous builds..."
mvn clean

echo ""
echo "Building JAR and Windows EXE..."
mvn package

echo ""
echo "========================================"
echo "Build Complete!"
echo "========================================"
echo ""
echo "Output files:"
echo "  - JAR: target/dev-tools-suite-1.0.0-executable.jar"
echo "  - EXE: target/dev-tools-suite.exe"
echo ""
echo "You can now distribute the .exe file!"
echo "Note: Users need Java 11 or higher installed."
echo ""
