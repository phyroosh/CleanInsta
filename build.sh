#!/usr/bin/env bash
set -e

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ANDROID_JAR="${ANDROID_JAR:-/home/phyroosh/development/android/platforms/android-34/android.jar}"
BUILD_TOOLS="${BUILD_TOOLS:-/home/phyroosh/development/android/build-tools/35.0.0}"
D8="${BUILD_TOOLS}/d8"
APKTOOL="${BASE_DIR}/build-tools/apktool.jar"

echo "[*] CleanInsta Build System"
echo "[*] Android SDK: ${ANDROID_JAR}"
echo "[*] Build Tools: ${BUILD_TOOLS}"

mkdir -p "${BASE_DIR}/out/stubs"
mkdir -p "${BASE_DIR}/out/classes"
mkdir -p "${BASE_DIR}/out/dex"

# 1. Compile stubs
echo "[*] Compiling stubs..."
javac --release 8 -cp "${ANDROID_JAR}" -d "${BASE_DIR}/out/stubs" $(find "${BASE_DIR}/stubs" -name "*.java")
jar cf "${BASE_DIR}/out/stubs.jar" -C "${BASE_DIR}/out/stubs" .

# 2. Compile CleanInsta sources
echo "[*] Compiling CleanInsta Java sources..."
find "${BASE_DIR}/src" -name "*.java" > "${BASE_DIR}/sources.txt"
javac --release 8 -cp "${ANDROID_JAR}:${BASE_DIR}/out/stubs.jar" -d "${BASE_DIR}/out/classes" @"${BASE_DIR}/sources.txt"

# 3. Convert to DEX with D8
echo "[*] Converting class files to DEX..."
"${D8}" --output "${BASE_DIR}/out/dex/clean_classes.jar" $(find "${BASE_DIR}/out/classes" -name "*.class")

# 4. Run patcher to inject and sign
echo "[*] Running APK patcher..."
python3 "${BASE_DIR}/build-tools/patcher.py"

echo "[+] Build complete: ${BASE_DIR}/out/CleanInsta_v1.0.apk"
