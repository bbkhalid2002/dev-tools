# Building Portable Executables - Quick Reference

## TL;DR - What Should I Use?

### For Windows Users (Most Common)

**Option 1: Simple .exe wrapper** (Recommended for most cases)
```bash
build-exe.bat
```
Output: `target/dev-tools-suite.exe` (~2-5 MB)
- Users need Java 11+ installed
- Small file size
- Easy to share

**Option 2: Full installer with bundled Java** (Best for end users)
```bash
build-jpackage.bat
```
Output: `target/installer/Dev Tools Suite-1.0.0.exe` (~50-100 MB)
- Users DON'T need Java installed
- Larger file size
- Professional installer experience
- Requires Java 14+ to build

---

## Detailed Build Instructions

### 1. Building Windows .exe (Launch4j)

**Prerequisites:**
- Java 11 or higher
- Maven installed

**Steps:**

Windows:
```bash
build-exe.bat
```

Linux/Mac:
```bash
./build-exe.sh
```

**Output:**
- Location: `target/dev-tools-suite.exe`
- Size: ~2-5 MB
- Type: .exe wrapper around JAR file

**How to distribute:**
1. Share the `dev-tools-suite.exe` file
2. User must have Java 11+ installed
3. Double-click to run

---

### 2. Building Self-Contained Installer (jpackage)

**Prerequisites:**
- Java 14 or higher (jpackage is included)
- Maven installed

**Steps:**

Windows (creates .exe installer):
```bash
build-jpackage.bat
```

Linux (creates .deb or .rpm):
```bash
./build-jpackage.sh
```

Mac (creates .dmg):
```bash
./build-jpackage.sh
```

**Output:**
- Location: `target/installer/`
- Size: ~50-100 MB
- Type: Native installer with bundled JRE

**How to distribute:**
1. Share the installer file from `target/installer/`
2. User runs installer (no Java needed)
3. Application installed with shortcuts

---

### 3. Building JAR Only (Cross-Platform)

**Steps:**
```bash
mvn clean package
```

**Output:**
- Location: `target/dev-tools-suite-1.0.0-executable.jar`
- Size: ~2-5 MB
- Type: Executable JAR

**How to distribute:**
1. Share the JAR file
2. User must have Java 11+ installed
3. Run with: `java -jar dev-tools-suite-1.0.0-executable.jar`

---

## Troubleshooting

### "jpackage not found"
- You need Java 14 or later
- Check: `java -version`
- Download from: https://adoptium.net/

### Launch4j build fails
- Check Maven can download plugins
- Try: `mvn clean install -U`

### Large file size with jpackage
- This is normal - includes full Java runtime
- Makes distribution easier (no Java needed)

---

## Quick Comparison

| Method | Size | User Needs Java? | Build Requires | Best For |
|--------|------|------------------|----------------|----------|
| JAR | 2-5 MB | Yes | Maven, Java 11+ | Developers |
| .exe (Launch4j) | 2-5 MB | Yes | Maven, Java 11+ | Windows users with Java |
| jpackage | 50-100 MB | No | Maven, Java 14+ | End users, production |

---

## Recommended Distribution Strategy

1. **For colleagues/developers:** Share the `.exe` or `.jar`
2. **For end users:** Create jpackage installer
3. **For GitHub releases:** Provide all three options

---

## Advanced: Custom jpackage Options

### Windows with custom icon:
```bash
jpackage --input target \
  --name "Dev Tools Suite" \
  --main-jar dev-tools-suite-1.0.0-executable.jar \
  --main-class com.devtoolssuite.ui.DevToolsSuiteGUI \
  --type exe \
  --icon path/to/icon.ico \
  --win-dir-chooser \
  --win-menu \
  --win-shortcut \
  --win-per-user-install \
  --app-version 1.0.0 \
  --dest target/installer
```

### Mac with custom icon:
```bash
jpackage --input target \
  --name "Dev Tools Suite" \
  --main-jar dev-tools-suite-1.0.0-executable.jar \
  --main-class com.devtoolssuite.ui.DevToolsSuiteGUI \
  --type dmg \
  --icon path/to/icon.icns \
  --mac-package-name "Jasypt UI" \
  --app-version 1.0.0 \
  --dest target/installer
```

### Linux .deb package:
```bash
jpackage --input target \
  --name "dev-tools-suite" \
  --main-jar dev-tools-suite-1.0.0-executable.jar \
  --main-class com.devtoolssuite.ui.DevToolsSuiteGUI \
  --type deb \
  --linux-shortcut \
  --app-version 1.0.0 \
  --dest target/installer
```
