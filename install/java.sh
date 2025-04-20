#!/bin/bash

# Java Installation Script for Fedora

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

# Check current Java version
check_java_version() {
    if command -v java &> /dev/null; then
        CURRENT_JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
        echo "Current Java version: $CURRENT_JAVA_VERSION"
    else
        echo "No Java installation found"
    fi
}

# Install OpenJDK 17
install_openjdk_17() {
    echo "Installing OpenJDK 17..."

    # Update package lists
    sudo dnf update -y

    # Install OpenJDK 17 Development Kit
    sudo dnf install -y java-17-openjdk-devel

    # Verify installation
    if [ $? -eq 0 ]; then
        print_status "OpenJDK 17 installed successfully"
    else
        print_error "Failed to install OpenJDK 17"
        return 1
    fi
}

# Configure Java Environment
configure_java_env() {
    echo "Configuring Java environment..."

    # Find JAVA_HOME automatically
    JAVA_HOME=$(readlink -f /usr/bin/java | sed "s:bin/java::")

    # Backup existing .bashrc
    cp ~/.bashrc ~/.bashrc.backup

    # Add Java configuration to .bashrc
    {
        echo ""
        echo "# Java Home Configuration"
        echo "export JAVA_HOME=$JAVA_HOME"
        echo "export PATH=\$JAVA_HOME/bin:\$PATH"
        echo "export JAVA_OPTS='-Xms256m -Xmx512m'"
    } >> ~/.bashrc

    # Source the updated .bashrc
    source ~/.bashrc

    print_status "Java environment configured"
}

# Verify Java Installation
verify_java_installation() {
    echo "Verifying Java installation..."

    # Check Java version
    java -version

    # Verify JAVA_HOME
    if [ -n "$JAVA_HOME" ]; then
        print_status "JAVA_HOME is set to: $JAVA_HOME"
    else
        print_error "JAVA_HOME is not set"
        return 1
    fi
}

# Main installation function
main() {
    echo "Starting Java Installation Process..."

    # Check current Java version
    check_java_version

    # Install OpenJDK 17
    if ! install_openjdk_17; then
        print_error "Java installation failed"
        exit 1
    fi

    # Configure Java environment
    configure_java_env

    # Verify installation
    verify_java_installation

    echo -e "${GREEN}Java Installation Complete!${NC}"
    echo "Recommended next steps:"
    echo "1. Verify Java installation with 'java -version'"
    echo "2. Check JAVA_HOME with 'echo $JAVA_HOME'"
    echo "3. Restart your terminal or run 'source ~/.bashrc'"
}

# Run the main function
main