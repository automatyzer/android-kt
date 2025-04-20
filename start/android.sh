#!/bin/bash

# Android App Startup Script

# Ensure we're in the project root directory
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

# Check for required dependencies
check_dependencies() {
    # Check Java
    if ! command -v java &> /dev/null; then
        echo "Error: Java is not installed. Please install OpenJDK 17."
        exit 1
    fi

    # Check Gradle
    if ! command -v gradle &> /dev/null && [ ! -x ./gradlew ]; then
        echo "Error: Gradle is not installed and gradlew is not executable."
        exit 1
    fi
}

# Clean previous builds
clean_project() {
    echo "Cleaning previous build..."
    if [ -x ./gradlew ]; then
        ./gradlew clean
    else
        gradle clean
    fi
}

# Build the project
build_project() {
    echo "Building the project..."
    if [ -x ./gradlew ]; then
        ./gradlew build
    else
        gradle build
    fi
}

# Run the app
run_app() {
    echo "Starting the Android app..."
    if [ -x ./gradlew ]; then
        ./gradlew run
    else
        gradle run
    fi
}

# Main execution
main() {
    check_dependencies
    clean_project
    build_project
    run_app
}

# Run the main function
main