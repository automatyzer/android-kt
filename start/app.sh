#!/bin/bash

# Android App Startup Script

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Print status message
print_status() {
    echo -e "${GREEN}[✓]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

print_error() {
    echo -e "${RED}[✗]${NC} $1"
}

# Detect Android SDK and Tools
detect_android_sdk() {
    # Common SDK locations
    SDK_LOCATIONS=(
        "$ANDROID_HOME"
        "$HOME/Android/Sdk"
        "$HOME/android-sdk"
        "/opt/android-sdk"
    )

    for location in "${SDK_LOCATIONS[@]}"; do
        if [ -d "$location" ] && [ -d "$location/emulator" ] && [ -d "$location/platform-tools" ]; then
            export ANDROID_HOME="$location"
            export PATH="$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools:$PATH"
            print_status "Android SDK found at $ANDROID_HOME"
            return 0
        fi
    done

    print_error "Android SDK not found. Please install Android Studio or set ANDROID_HOME"
    return 1
}

# Prepare Android Emulator
prepare_emulator() {
    echo "Checking for available Android emulators..."

    # Ensure emulator command is available
    if ! command -v emulator &> /dev/null; then
        print_error "Android emulator command not found"
        return 1
    }

    # List available emulators
    EMULATORS=$(emulator -list-avds)

    if [ -z "$EMULATORS" ]; then
        print_error "No Android emulators found. Create an emulator using Android Studio AVD Manager."
        return 1
    fi

    echo "Available emulators:"
    echo "$EMULATORS"

    # Select first available emulator
    SELECTED_EMULATOR=$(echo "$EMULATORS" | head -n 1)

    if [ -z "$SELECTED_EMULATOR" ]; then
        print_error "Could not select an emulator"
        return 1
    fi

    print_status "Selected emulator: $SELECTED_EMULATOR"
    return 0
}

# Start Android Emulator
start_emulator() {
    if ! prepare_emulator; then
        print_warning "Skipping emulator startup"
        return 1
    fi

    echo "Starting Android Emulator: $SELECTED_EMULATOR"

    # Kill any existing emulator instances
    pkill -f "qemu-system-x86_64"

    # Start emulator in background with verbose logging
    emulator -avd "$SELECTED_EMULATOR" -no-snapshot-load -no-audio &

    # Wait for emulator to boot
    echo "Waiting for emulator to boot..."

    # Timeout after 5 minutes (300 seconds)
    timeout 300 bash -c '
        while true; do
            if adb shell "getprop sys.boot_completed" 2>/dev/null | grep -q "1"; then
                exit 0
            fi
            sleep 5
        done
    '

    if [ $? -eq 0 ]; then
        print_status "Emulator booted successfully"
        return 0
    else
        print_error "Emulator failed to boot within 5 minutes"
        return 1
    fi
}

# Check Prerequisites
check_prerequisites() {
    # Check Java
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed"
        return 1
    fi

    # Detect Android SDK
    if ! detect_android_sdk; then
        return 1
    fi

    # Check Gradle
    if ! command -v gradle &> /dev/null && [ ! -x ./gradlew ]; then
        print_error "Gradle is not installed and gradlew is not executable"
        return 1
    fi

    return 0
}

# Build and Install App
build_and_install() {
    echo "Building and installing the app..."

    # Use Gradle wrapper for consistent builds
    if [ -x ./gradlew ]; then
        # Clean and build the project
        ./gradlew clean build

        if [ $? -ne 0 ]; then
            print_error "Project build failed"
            return 1
        fi

        # Install debug variant to connected device/emulator
        ./gradlew :app:installDebug

        if [ $? -ne 0 ]; then
            print_error "App installation failed"
            return 1
        fi
    else
        print_error "Gradle wrapper not found or not executable"
        return 1
    fi
}

# Run the App
run_app() {
    echo "Launching the app..."

    # Get package name from app/build.gradle
    PACKAGE_NAME=$(grep "applicationId" app/build.gradle | awk -F '"' '{print $2}')

    if [ -z "$PACKAGE_NAME" ]; then
        print_error "Could not determine app package name"
        return 1
    fi

    # Start the app
    adb shell am start -n "$PACKAGE_NAME/.MainActivity"

    if [ $? -eq 0 ]; then
        print_status "App launched"
    else
        print_error "Failed to launch app"
        return 1
    fi
}

# Main execution
main() {
    echo "Starting Android App Development Environment"

    # Change to project root directory
    cd "$(dirname "$0")/.." || exit 1

    # Check prerequisites
    if ! check_prerequisites; then
        print_error "Prerequisites check failed"
        exit 1
    fi

    # Start emulator (optional)
    if ! start_emulator; then
        print_warning "Emulator startup failed. Attempting to continue with connected device."
    fi

    # Build and install app
    if ! build_and_install; then
        print_error "Build or installation failed"
        exit 1
    fi

    # Run the app
    if ! run_app; then
        print_error "Failed to run the app"
        exit 1
    fi

    echo -e "${GREEN}Android App Startup Complete!${NC}"
}

# Run the main function
main