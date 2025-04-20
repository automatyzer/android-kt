#!/bin/bash

# Android Development Environment Setup Script for Fedora

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print status messages
print_status() {
    echo -e "${GREEN}[✓]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

print_error() {
    echo -e "${RED}[✗]${NC} $1"
}

# System Update and Preparation
system_prep() {
    echo "Preparing system for Android development..."
    sudo dnf update -y
    sudo dnf install -y wget curl unzip which
    print_status "System updated and basic tools installed"
}

# Install Additional Development Tools
install_dev_tools() {
    echo "Installing additional development tools..."

    # Install essential build tools
    sudo dnf install -y \
        make \
        cmake \
        gcc \
        gcc-c++ \
        git \
        glibc-devel \
        libstdc++-devel \
        zlib-devel \
        ncurses-devel

    print_status "Development tools installed"
}

# Download and Install Android Studio
download_android_studio() {
    echo "Downloading Android Studio..."

    # Create downloads and installation directories
    mkdir -p ~/Downloads/android-studio
    mkdir -p ~/opt
    cd ~/Downloads/android-studio

    # URL for the latest stable version
    ANDROID_STUDIO_URL="https://redirector.gvt1.com/edgedl/android/studio/ide-zips/2023.2.1.24/android-studio-2023.2.1.24-linux.tar.gz"

    # Download Android Studio
    wget -O android-studio.tar.gz "$ANDROID_STUDIO_URL"

    # Extract Android Studio
    tar -xzf android-studio.tar.gz -C ~/opt

    print_status "Android Studio downloaded and extracted to ~/opt/android-studio"
}

# Set up Android SDK Environment Variables
configure_android_env() {
    echo "Configuring Android SDK environment variables..."

    # Create Android SDK directory
    mkdir -p ~/Android/Sdk

    # Backup existing .bashrc
    cp ~/.bashrc ~/.bashrc.backup

    # Add Android configuration to .bashrc
    {
        echo ""
        echo "# Android SDK Configuration"
        echo "export ANDROID_HOME=~/Android/Sdk"
        echo "export ANDROID_SDK_ROOT=~/Android/Sdk"
        echo "export PATH=\$PATH:\$ANDROID_HOME/tools"
        echo "export PATH=\$PATH:\$ANDROID_HOME/platform-tools"
        echo "export PATH=\$PATH:\$ANDROID_HOME/tools/bin"
        echo "export PATH=\$PATH:~/opt/android-studio/bin"
    } >> ~/.bashrc

    # Source the updated .bashrc
    source ~/.bashrc

    print_status "Android SDK environment variables configured"
}

# Install Android SDK Components
install_sdk_components() {
    echo "Installing Android SDK components..."

    # Find the correct path for sdkmanager
    CMDLINE_TOOLS_DIR=~/opt/android-studio/plugins/android/resources/templates/gradle/wrapper/dists/
    CMDLINE_TOOLS_PATHS=(
        "~/opt/android-studio/cmdline-tools/bin/sdkmanager"
        "~/opt/android-studio/cmdline-tools/latest/bin/sdkmanager"
        "$(find "$CMDLINE_TOOLS_DIR" -name "sdkmanager" | head -n 1)"
    )

    SDKMANAGER=""
    for path in "${CMDLINE_TOOLS_PATHS[@]}"; do
        expanded_path=$(eval echo "$path")
        if [ -x "$expanded_path" ]; then
            SDKMANAGER="$expanded_path"
            break
        fi
    done

    if [ -z "$SDKMANAGER" ]; then
        print_error "sdkmanager not found. Please manually download and install Android SDK tools."
        return 1
    fi

    # Create Android SDK directory if it doesn't exist
    mkdir -p ~/Android/Sdk/cmdline-tools/latest

    # Install SDK components
    "$SDKMANAGER" \
        "platform-tools" \
        "platforms;android-34" \
        "build-tools;34.0.0" \
        "cmdline-tools;latest" \
        "extras;intel;Hardware_Accelerated_Execution_Manager"

    print_status "Android SDK components installed"
}

# Verify Installation
verify_installation() {
    echo "Verifying Android development environment..."

    # Check Java
    java -version

    # Check Android Studio
    if [ -f ~/opt/android-studio/bin/studio.sh ]; then
        print_status "Android Studio is installed"
    else
        print_warning "Android Studio installation not found"
    fi

    # Check SDK tools
    SDK_TOOLS_PATHS=(
        "~/Android/Sdk/platform-tools/adb"
        "~/opt/android-studio/platform-tools/adb"
    )

    SDK_TOOLS_FOUND=false
    for path in "${SDK_TOOLS_PATHS[@]}"; do
        expanded_path=$(eval echo "$path")
        if [ -x "$expanded_path" ]; then
            SDK_TOOLS_FOUND=true
            break
        fi
    done

    if [ "$SDK_TOOLS_FOUND" = true ]; then
        print_status "SDK tools are available"
    else
        print_warning "SDK tools not found in PATH"
    fi
}

# Main Setup Function
main() {
    echo "Starting Android Development Environment Setup..."

    system_prep
    install_dev_tools
    download_android_studio
    configure_android_env
    install_sdk_components
    verify_installation

    echo -e "${GREEN}Android Development Environment Setup Complete!${NC}"
    echo "Next steps:"
    echo "1. Open Android Studio for first-time setup"
    echo "2. Configure IDE and additional SDK components"
    echo "3. Start developing Android applications"
}

# Run the main setup function
main