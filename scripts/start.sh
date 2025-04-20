#!/bin/bash
# Script to start the Android Kotlin app using the correct Java version

# Set this to your installed Java 17 path if available
default_java_home="/usr/lib/jvm/java-17"

if [ ! -d "$JAVA_HOME" ]; then
  if [ -d "$default_java_home" ]; then
    export JAVA_HOME="$default_java_home"
    echo "Using JAVA_HOME: $JAVA_HOME"
  else
    echo "Java 17 not found. Please install OpenJDK 17 and set JAVA_HOME."
    exit 1
  fi
fi

cd "$(dirname "$0")/.."

./gradlew :app:installDebug
./gradlew build