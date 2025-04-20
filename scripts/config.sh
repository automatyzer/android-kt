#!/bin/bash

# Gradle and Java Compatibility Diagnostic Script

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

# Check Java Version
check_java_version() {
    echo "Checking Java Version..."
    java -version
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')

    # Extract major version number
    MAJOR_VERSION=$(echo "$JAVA_VERSION" | cut -d. -f1)

    echo "Java Major Version: $MAJOR_VERSION"
    return 0
}

# Check Gradle Version
check_gradle_version() {
    echo "Checking Gradle Version..."
    gradle --version
}

# Update Gradle Wrapper
update_gradle_wrapper() {
    echo "Updating Gradle Wrapper..."

    # Determine the appropriate Gradle version for Java 21
    GRADLE_VERSION="8.2.2"

    # Create or update gradle-wrapper.properties
    WRAPPER_PROPS="gradle/wrapper/gradle-wrapper.properties"

    if [ ! -f "$WRAPPER_PROPS" ]; then
        mkdir -p gradle/wrapper
    fi

    # Update wrapper properties
    cat > "$WRAPPER_PROPS" << EOL
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOL

    # Update the Gradle wrapper
    ./gradlew wrapper --gradle-version=$GRADLE_VERSION

    print_status "Gradle Wrapper updated to version $GRADLE_VERSION"
}

# Create Settings Gradle
create_settings_gradle() {
    SETTINGS_GRADLE="settings.gradle"

    cat > "$SETTINGS_GRADLE" << EOL
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex "com\\.android.*"
                includeGroupByRegex "com\\.google.*"
                includeGroupByRegex "androidx.*"
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = 'android-kt'
include ':app'
EOL

    print_status "Created comprehensive settings.gradle"
}

# Update build.gradle for Java compatibility
update_build_gradle() {
    # Top-level build.gradle
    TOP_GRADLE="build.gradle"

    # Backup existing file
    cp "$TOP_GRADLE" "${TOP_GRADLE}.backup"

    # Update build.gradle
    cat > "$TOP_GRADLE" << EOL
// Top-level build file for project-wide configurations

buildscript {
    ext {
        // Version management for key libraries
        kotlin_version = '1.9.24'
        android_gradle_plugin_version = '8.2.2'
        hilt_version = '2.48.1'
        navigation_version = '2.7.6'
        compose_compiler_version = '1.5.1'
    }

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
        classpath "com.android.tools.build:gradle:\$android_gradle_plugin_version"
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:\$kotlin_version"
        classpath "com.google.dagger:hilt-android-gradle-plugin:\$hilt_version"
        classpath "androidx.navigation:navigation-safe-args-gradle-plugin:\$navigation_version"
    }
}

// Direct plugin management
plugins {
    // Use the exact plugin version from the classpath
    id 'com.android.application' version "${android_gradle_plugin_version}" apply false
    id 'org.jetbrains.kotlin.android' version "\$kotlin_version" apply false
    id 'com.google.dagger.hilt.android' version "\$hilt_version" apply false
    id 'androidx.navigation.safeargs.kotlin' version "\$navigation_version" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

tasks.register('clean', Delete) {
    delete rootProject.buildDir
}

// Configure global settings for Java compatibility
subprojects {
    afterEvaluate { project ->
        if (project.hasProperty('android')) {
            project.android.compileOptions {
                sourceCompatibility JavaVersion.VERSION_17
                targetCompatibility JavaVersion.VERSION_17
            }

            project.android.kotlinOptions {
                jvmTarget = '17'
            }
        }
    }
}
EOL

    print_status "Updated top-level build.gradle for Java compatibility"
}

# Cleanup Gradle Caches
cleanup_gradle_caches() {
    echo "Cleaning Gradle Caches..."

    # Stop running Gradle daemons
    ./gradlew --stop

    # Remove Gradle caches
    rm -rf ~/.gradle/caches/

    print_status "Gradle caches cleaned"
}

# Main execution
main() {
    echo "Starting Gradle Compatibility Diagnostic and Fix"

    # Check current Java and Gradle versions
    check_java_version
    check_gradle_version

    # Create comprehensive settings.gradle
    create_settings_gradle

    # Update Gradle wrapper
    update_gradle_wrapper

    # Update build.gradle
    update_build_gradle

    # Cleanup Gradle caches
    cleanup_gradle_caches

    # Sync project
    ./gradlew clean

    echo -e "${GREEN}Gradle Compatibility Diagnostic and Fix Complete!${NC}"
    echo "Next steps:"
    echo "1. Review the updated Gradle configuration"
    echo "2. Sync project in Android Studio"
    echo "3. Rebuild the project"
}

# Run the main function
main