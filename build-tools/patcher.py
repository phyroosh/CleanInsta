#!/usr/bin/env python3
import os
import sys
import zipfile
import subprocess
import shutil

BASE_DIR = "/home/phyroosh/.gemini/antigravity/scratch/clean-insta"
SRC_APK = "/home/phyroosh/Downloads/ABDM/Programs/Honista_v13.0__x64.apk"
CLEAN_DEX = os.path.join(BASE_DIR, "out/dex/classes_clean19.dex")
OUT_DIR = os.path.join(BASE_DIR, "out")
UNALIGNED_APK = os.path.join(OUT_DIR, "clean_unaligned.apk")
ALIGNED_APK = os.path.join(OUT_DIR, "clean_aligned.apk")
FINAL_APK = os.path.join(OUT_DIR, "CleanInsta_v1.0.apk")

BUILD_TOOLS = "/home/phyroosh/development/android/build-tools/35.0.0"
ZIPALIGN = os.path.join(BUILD_TOOLS, "zipalign")
APKSIGNER = os.path.join(BUILD_TOOLS, "apksigner")
KEYSTORE = "/home/phyroosh/.android/debug.keystore"

print(f"[*] Repacking {SRC_APK} with clean DEX...")
if not os.path.exists(CLEAN_DEX):
    print(f"[!] Clean DEX not found at {CLEAN_DEX}")
    sys.exit(1)

with open(CLEAN_DEX, "rb") as f:
    clean_dex_bytes = f.read()
print(f"[*] Clean DEX size: {len(clean_dex_bytes)} bytes")

if os.path.exists(UNALIGNED_APK):
    os.remove(UNALIGNED_APK)

with zipfile.ZipFile(SRC_APK, "r") as src_zip, \
     zipfile.ZipFile(UNALIGNED_APK, "w") as out_zip:
    
    total = len(src_zip.infolist())
    count = 0
    for item in src_zip.infolist():
        count += 1
        name = item.filename
        
        # Skip old signature files
        if name.startswith("META-INF/") and (name.endswith(".SF") or name.endswith(".RSA") or name.endswith(".DSA") or name.endswith(".MF")):
            continue
            
        # Replace classes19.dex with our clean bytecode
        if name == "classes19.dex":
            print("[+] Injected clean classes19.dex replacement")
            out_zip.writestr("classes19.dex", clean_dex_bytes, compress_type=zipfile.ZIP_DEFLATED)
            continue
            
        # Copy original entry with same compression
        data = src_zip.read(name)
        out_zip.writestr(item, data)

print(f"[*] Created {UNALIGNED_APK}")

# Align APK
print("[*] Running zipalign...")
if os.path.exists(ALIGNED_APK):
    os.remove(ALIGNED_APK)

res = subprocess.run([ZIPALIGN, "-p", "-f", "4", UNALIGNED_APK, ALIGNED_APK], capture_output=True, text=True)
if res.returncode != 0:
    print(f"[!] zipalign failed:\n{res.stderr}")
    sys.exit(1)
print(f"[+] Aligned APK created at {ALIGNED_APK}")

# Sign APK
print("[*] Signing APK with debug keystore...")
if os.path.exists(FINAL_APK):
    os.remove(FINAL_APK)

cmd = [
    APKSIGNER, "sign",
    "--ks", KEYSTORE,
    "--ks-pass", "pass:android",
    "--key-pass", "pass:android",
    "--ks-key-alias", "androiddebugkey",
    "--out", FINAL_APK,
    ALIGNED_APK
]
res = subprocess.run(cmd, capture_output=True, text=True)
if res.returncode != 0:
    print(f"[!] apksigner sign failed:\n{res.stderr}\n{res.stdout}")
    sys.exit(1)

print(f"[+] Successfully generated signed APK: {FINAL_APK}")

# Verify APK
res = subprocess.run([APKSIGNER, "verify", "--verbose", FINAL_APK], capture_output=True, text=True)
if res.returncode == 0:
    print("[+] APK signature verified successfully:")
    for line in res.stdout.splitlines()[:5]:
        print("   ", line)
else:
    print(f"[!] Signature verification failed:\n{res.stderr}")
