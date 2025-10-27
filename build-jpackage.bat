@echo off
echo ========================================
echo Building Native Installer with jpackage
echo ========================================
echo.
echo Note: This requires Java 14+ with jpackage
echo.

echo Cleaning and building JAR...
call mvn clean package

echo.
echo Creating native Windows installer...
jpackage ^
  --input target ^
  --name "Dev Tools Suite" ^
  --main-jar dev-tools-suite-1.0.0-executable.jar ^
  --main-class com.devtoolssuite.ui.DevToolsSuiteGUI ^
  --icon icons\dev_tools_suite.ico ^
  --type exe ^
  --win-shortcut ^
  --win-menu ^
  --app-version 1.0.0 ^
  --description "AES-GCM Encryption/Decryption Tool" ^
  --vendor "Dev Tools Suite" ^
  --dest target/installer

echo.
echo ========================================
echo Build Complete!
echo ========================================
echo.
echo Installer created in: target\installer\
echo.
echo This installer includes a bundled JRE,
echo so users don't need Java installed!
echo.
pause
