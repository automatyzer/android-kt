#!/bin/bash
# start/app.sh
# Gradle Wrapper Regeneration Script

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

# Validate Gradle Installation
validate_gradle() {
    # Check if Gradle is installed
    if ! command -v gradle &> /dev/null; then
        print_error "Gradle is not installed. Installing Gradle..."

        # Attempt to install Gradle
        if command -v sdk &> /dev/null; then
            sdk install gradle
        elif command -v dnf &> /dev/null; then
            sudo dnf install gradle -y
        else
            print_error "Unable to install Gradle. Please install manually."
            return 1
        fi
    fi

    print_status "Gradle is installed"
    return 0
}

# Regenerate Gradle Wrapper
regenerate_gradle_wrapper() {
    local GRADLE_VERSION="8.5"

    # Ensure we're in the project root
    cd "$(dirname "$0")/.." || exit 1

    # Remove existing Gradle wrapper if it exists
    rm -f gradlew gradlew.bat
    rm -rf gradle/wrapper

    # Create Gradle wrapper directories
    mkdir -p gradle/wrapper

    # Regenerate Gradle wrapper
    echo "Regenerating Gradle wrapper..."
    gradle wrapper --gradle-version=$GRADLE_VERSION

    if [ $? -eq 0 ]; then
        print_status "Gradle wrapper regenerated successfully"

        # Ensure wrapper is executable
        chmod +x gradlew
        print_status "Gradle wrapper made executable"
    else
        print_error "Failed to regenerate Gradle wrapper"
        return 1
    fi
}

# Verify Gradle Wrapper
verify_gradle_wrapper() {
    # Check if gradlew exists and is executable
    if [ -x ./gradlew ]; then
        print_status "Gradle wrapper is present and executable"

        # Display Gradle version
        ./gradlew --version
    else
        print_error "Gradle wrapper is missing or not executable"
        return 1
    fi
}

# Main execution
main() {
    echo "Starting Gradle Wrapper Regeneration"

    # Validate Gradle installation
    if ! validate_gradle; then
        print_error "Gradle validation failed"
        exit 1
    fi

    # Regenerate Gradle wrapper
    if ! regenerate_gradle_wrapper; then
        print_error "Gradle wrapper regeneration failed"
        exit 1
    fi

    # Verify Gradle wrapper
    if ! verify_gradle_wrapper; then
        print_error "Gradle wrapper verification failed"
        exit 1
    fi

    echo -e "${GREEN}Gradle Wrapper Regeneration Complete!${NC}"
    echo "Next steps:"
    echo "1. Run './gradlew build' to verify"
    echo "2. Sync project in Android Studio"
}

# Run the main function
main