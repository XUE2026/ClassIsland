#!/bin/bash
set -e
cd "$(dirname "$0")"
echo "Building Rust native libraries for Android..."
TARGETS=("aarch64-linux-android" "armv7-linux-androideabi" "x86_64-linux-android" "i686-linux-android")
ABIS=("arm64-v8a" "armeabi-v7a" "x86_64" "x86")
for i in "${!TARGETS[@]}"; do
    echo "Building ${TARGETS[$i]}..."
    cargo build --target "${TARGETS[$i]}" --release 2>/dev/null && \
    mkdir -p "../app/src/main/cpp/libs/${ABIS[$i]}" && \
    cp "target/${TARGETS[$i]}/release/libclassisland_rust.so" "../app/src/main/cpp/libs/${ABIS[$i]}/" && \
    echo "Copied ${ABIS[$i]}"
done
echo "Done"