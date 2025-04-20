#!/bin/bash
# Script to set up environment for Kotlin Android app
set -e

# Check for Java (required for Gradle)
if ! command -v java >/dev/null 2>&1; then
  echo "Java is not installed. Please install JDK 11 or newer."
  exit 1
fi

# Check for Android SDK
if [ -z "$ANDROID_HOME" ]; then
  echo "ANDROID_HOME is not set. Please set up Android SDK and ANDROID_HOME."
  exit 1
fi

# Check for Gradle Wrapper
if [ ! -f ./gradlew ]; then
  echo "Gradle wrapper not found. Creating gradlew..."
  cp scripts/gradlew_template ./gradlew
  chmod +x ./gradlew
fi

# Install dependencies
./gradlew --no-daemon build --stacktrace

echo "Environment setup complete."
