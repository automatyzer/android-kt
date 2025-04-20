#!/bin/bash

# OpenJDK 17 Installation and Environment Setup Script for Fedora

# Update system packages
sudo dnf update -y

# Install OpenJDK 17 Development Kit
sudo dnf install -y java-17-openjdk-devel

# Verify Java installation
java -version

# Find JAVA_HOME automatically
JAVA_HOME=$(readlink -f /usr/bin/java | sed "s:bin/java::")

# Add JAVA_HOME to user's .bashrc
echo "# Java Home Configuration" >> ~/.bashrc
echo "export JAVA_HOME=$JAVA_HOME" >> ~/.bashrc
echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> ~/.bashrc

# Source the updated .bashrc
source ~/.bashrc

# Verify JAVA_HOME is set correctly
echo "JAVA_HOME is set to: $JAVA_HOME"

# Additional Android development recommendations
echo "
# Recommended next steps for Android development:
# 1. Install Android Studio from https://developer.android.com/studio
# 2. Install additional Android SDK tools and platforms
# 3. Configure Android Studio to use the installed OpenJDK 17
"