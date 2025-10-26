@echo off
echo ========================================
echo Building Jasypt UI Executable
echo ========================================
echo.

echo Cleaning previous builds...
call mvn clean

echo.
echo Building JAR and Windows EXE...
call mvn package

echo.
echo ========================================
echo Build Complete!
echo ========================================
echo.
echo Output files:
echo   - JAR: target\jasypt-ui-1.0.0-executable.jar
echo   - EXE: target\jasypt-ui.exe
echo.
echo You can now distribute the .exe file!
echo Note: Users need Java 11 or higher installed.
echo.
pause
