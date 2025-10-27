# Dev Tools Suite - AES-GCM Encryption/Decryption GUI

A Java Swing GUI application for encrypting and decrypting text using AES-GCM encryption.

## Features

- **Encrypt** plain text using AES-GCM encryption
- **Decrypt** encrypted text back to plain text
- Configurable algorithm, password, and salt
- Copy output to clipboard
- Clean and intuitive user interface

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

## Building the Application

### Quick Build (JAR files)

To build the application, run:

```bash
mvn clean package
```

This will create files in the `target` directory:
- `dev-tools-suite-1.0.0.jar` - Regular JAR
- `dev-tools-suite-1.0.0-executable.jar` - Executable JAR with all dependencies

### Building Portable Executables

#### Option 1: Windows .exe (Requires Java installed on user's PC)

**Using the build script (recommended):**
```bash
# Windows
build-exe.bat

# Linux/Mac
./build-exe.sh
```

**Or manually:**
```bash
mvn clean package
```

This creates `target/dev-tools-suite.exe` - a Windows executable that can be distributed to users.

**Pros:**
- Small file size (wraps the JAR)
- Double-click to run on Windows
- Professional .exe file

**Cons:**
- Users need Java 11+ installed on their system

#### Option 2: Self-Contained Installer (Includes Java - No Java installation needed!)

**Using the build script (recommended):**
```bash
# Windows
build-jpackage.bat

# Linux/Mac
./build-jpackage.sh
```

**Or manually:**
```bash
mvn clean package
jpackage --input target \
   --name "Dev Tools Suite" \
   --main-jar dev-tools-suite-1.0.0-executable.jar \
   --main-class com.devtoolssuite.ui.DevToolsSuiteGUI \
  --type exe \
  --win-shortcut \
  --win-menu \
  --app-version 1.0.0 \
  --dest target/installer
```

This creates a native installer in `target/installer/` with a bundled Java runtime.

**Pros:**
- Users don't need Java installed
- True portable application
- Can create installers for Windows (.exe), Mac (.dmg), or Linux (.deb, .rpm)

**Cons:**
- Larger file size (~50-100 MB)
- Requires Java 14+ to build

**Note:** jpackage is included with JDK 14 and later.

## Running the Application

### Option 1: Run with Maven
```bash
mvn compile exec:java -Dexec.mainClass="com.devtoolssuite.ui.DevToolsSuiteGUI"
```

### Option 2: Run the executable JAR
```bash
java -jar target/dev-tools-suite-1.0.0-executable.jar
```

### Option 3: Run the Windows .exe
```bash
# Simply double-click the .exe file, or:
target\dev-tools-suite.exe
```

### Option 4: Install and run using jpackage installer
1. Run the installer created in `target/installer/`
2. Launch from Start Menu or Desktop shortcut

## Usage

1. **Configure Encryption Parameters:**
   - **Algorithm**: The secret key algorithm (default: `PBKDF2WithHmacSHA256`)
   - **Password**: Your secret key password
   - **Salt**: Your secret key salt

2. **Encrypt Text:**
   - Enter the plain text in the "Input Text" area
   - Click the "Encrypt" button in the toolbar
   - The encrypted text will appear in the "Output Text" area

3. **Decrypt Text:**
   - Enter the encrypted text in the "Input Text" area
   - Click the "Decrypt" button in the toolbar
   - The decrypted plain text will appear in the "Output Text" area

4. **Additional Actions:**
   - **Clear**: Clears both input and output text areas
   - **Copy Output**: Copies the output text to your clipboard

## Configuration

The application uses the same encryption configuration that was previously documented:
- **Secret Key Iterations**: 1000
- **Default Algorithm**: PBKDF2WithHmacSHA256
- **Encryption Mode**: AES-GCM (via SimpleGCMStringEncryptor)

## Dependencies

- [Jasypt Spring Boot](https://github.com/ulisesbocchio/jasypt-spring-boot) - Version 3.0.5
- Spring Core - Version 5.3.31
- Spring Context - Version 5.3.31

## Project Structure
```
dev-tools-suite/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── devtoolssuite/
│                   └── ui/
│                       ├── DevToolsSuiteGUI.java       # Main GUI application
│                       └── DevToolsSuiteEncryptor.java # Encryption utility class
├── pom.xml                                         # Maven configuration
├── build-exe.bat                                   # Windows script to build .exe
├── build-exe.sh                                    # Linux/Mac script to build .exe
├── build-jpackage.bat                              # Windows script to build installer
├── build-jpackage.sh                               # Linux/Mac script to build installer
├── .gitignore                                      # Git ignore file
└── README.md                                       # This file
```

## Distribution Options Comparison

| Option | File Size | Requires Java on User PC | Best For |
|--------|-----------|-------------------------|----------|
| JAR file | ~2-5 MB | Yes (Java 11+) | Developers, Java users |
| Windows .exe | ~2-5 MB | Yes (Java 11+) | Windows users with Java |
| jpackage installer | ~50-100 MB | No (bundled) | End users, production |

**Recommendation for sharing:**
- **With technical users:** Share the `.exe` file (small, easy to run)
- **With non-technical users:** Share the jpackage installer (no Java needed)
- **Cross-platform:** Share the JAR file (works on all platforms)

## Notes

- Make sure to keep your password and salt secure
- The same password, salt, and algorithm must be used for both encryption and decryption
- Encrypted values are Base64 encoded strings
