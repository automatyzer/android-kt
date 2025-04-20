#!/bin/bash

# Gradle Configuration Diagnostic and Fix Script

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

# Check and update Gradle configuration
update_gradle_config() {
    # Top-level build.gradle
    TOP_LEVEL_GRADLE="build.gradle"
    TOP_LEVEL_BACKUP="build.gradle.backup"

    # App module build.gradle
    APP_GRADLE="app/build.gradle"
    APP_BACKUP="app/build.gradle.backup"

    # Create backups
    if [ -f "$TOP_LEVEL_GRADLE" ]; then
        cp "$TOP_LEVEL_GRADLE" "$TOP_LEVEL_BACKUP"
        print_status "Created backup of top-level build.gradle"
    fi

    if [ -f "$APP_GRADLE" ]; then
        cp "$APP_GRADLE" "$APP_BACKUP"
        print_status "Created backup of app module build.gradle"
    fi

    # Create/update top-level build.gradle
    cat > "$TOP_LEVEL_GRADLE" << EOL
// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    ext {
        // Define version variables for easier management
        kotlin_version = "1.9.22"
        nav_version = "2.7.7"
        gradle_version = "8.2.2"
    }

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:\$kotlin_version"
        classpath "androidx.navigation:navigation-safe-args-gradle-plugin:\$nav_version"
        classpath "com.android.tools.build:gradle:\$gradle_version"
    }
}

plugins {
    id 'com.android.application' version '8.2.2' apply false
    id 'org.jetbrains.kotlin.android' version '1.9.22' apply false
    id 'androidx.navigation.safeargs.kotlin' version '2.7.7' apply false
}

tasks.register('clean', Delete) {
    delete rootProject.buildDir
}
EOL

    # Create/update app module build.gradle
    cat > "$APP_GRADLE" << EOL
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'androidx.navigation.safeargs.kotlin'
}

android {
    namespace 'com.example.androidkt'
    compileSdk 34

    defaultConfig {
        applicationId "com.example.androidkt"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
        debug {
            debuggable true
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = '17'
    }

    buildFeatures {
        viewBinding true
        dataBinding true
    }
}

dependencies {
    // Kotlin
    implementation "org.jetbrains.kotlin:kotlin-stdlib:1.9.22"
    implementation "org.jetbrains.kotlin:kotlin-reflect:1.9.22"

    // AndroidX Core
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'

    // Navigation
    implementation "androidx.navigation:navigation-fragment-ktx:2.7.7"
    implementation "androidx.navigation:navigation-ui-ktx:2.7.7"

    // Lifecycle
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'

    // Testing
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
EOL

    print_status "Updated Gradle configuration files"
}

# Clean and rebuild Gradle project
rebuild_project() {
    echo "Cleaning and rebuilding project..."

    # Clean Gradle caches
    ./gradlew clean

    # Rebuild project
    ./gradlew build

    # Check build status
    if [ $? -eq 0 ]; then
        print_status "Project rebuilt successfully"
    else
        print_error "Project rebuild failed"
        return 1
    fi
}

# Main execution
main() {
    echo "Starting Gradle Configuration Diagnostic and Fix"

    # Update Gradle configuration
    update_gradle_config

    # Rebuild project
    if ! rebuild_project; then
        print_error "Failed to rebuild project"
        exit 1
    fi

    echo -e "${GREEN}Gradle Configuration Diagnostic and Fix Complete!${NC}"
    echo "Next steps:"
    echo "1. Review the updated build.gradle files"
    echo "2. Run './gradlew :app:installDebug' to install the app"
}

# Run the main function
main