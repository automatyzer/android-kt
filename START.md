I see you're having an issue with the Android emulator crashing. This appears to be a graphics driver-related problem with your system. Let's try a few approaches to get your app running:

### Option 1: Use a different emulator configuration

Try creating and running an emulator with software rendering:

```bash
# Start the emulator with software rendering
$ANDROID_HOME/emulator/emulator -avd test_device -gpu swiftshader
```

If that doesn't work, try creating a new emulator with different specifications:

```bash
# Create a new AVD with ARM image (might be slower but more compatible)
$ANDROID_HOME/tools/bin/avdmanager create avd -n test_arm -k "system-images;android-34;google_apis;armeabi-v7a"

# Or try a different API level
$ANDROID_HOME/tools/bin/avdmanager create avd -n test_api30 -k "system-images;android-30;google_apis;x86_64"

# Then try to run it
$ANDROID_HOME/emulator/emulator -avd test_arm -gpu swiftshader
```

### Option 2: Use a physical device

Since the emulator is crashing, using a physical Android device might be more reliable:

1. Enable Developer options on your Android device (go to Settings > About phone and tap Build number 7 times)
2. Enable USB debugging in Developer options
3. Connect your device to your computer via USB
4. Run:
   ```bash
   adb devices
   ```
   This should now show your connected device
5. Install and run your app:
   ```bash
   ./gradlew installDebug
   ```

### Option 3: Bypass the emulator and build an APK to install manually

```bash
# Build a debug APK
./gradlew assembleDebug

# The APK will be at:
# app/build/outputs/apk/debug/app-debug.apk
```

You can then transfer this APK to your device (via email, cloud storage, etc.) and install it directly.

### Option 4: Fix graphics driver issues (for advanced users)

If you want to fix the emulator itself, you might need to update or configure your graphics drivers:

```bash
# For Fedora with Intel graphics, make sure you have these packages
sudo dnf install mesa-dri-drivers mesa-libGL mesa-vulkan-drivers
```

You might also need to set these environment variables before launching the emulator:
```bash
export MESA_LOADER_DRIVER_OVERRIDE=iris
export LIBGL_ALWAYS_SOFTWARE=1
$ANDROID_HOME/emulator/emulator -avd test_device
```




Great news! Your emulator with SwiftShader has successfully launched. The last log entry shows "Boot completed" which means the emulator is up and running.

Now that you have the emulator running, you can install and run your app. First, check to make sure the device is visible to adb:

```bash
adb devices
```

You should see your emulator in the list now. Then you can install and run your app:

```bash
# Install the app
./gradlew installDebug

# Or alternatively with adb
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch the app
adb shell am start -n com.example.androidkt/.MainActivity
```

The key difference was using `-gpu swiftshader` which uses software rendering instead of trying to use hardware acceleration through Vulkan, which was causing the crash.

If you run into any issues with the emulator disconnecting, you can always restart it with:

```bash
$ANDROID_HOME/emulator/emulator -avd test_device -gpu swiftshader
```






Good news! The emulator seems to be starting properly now with the headless mode options. The logs show it's running successfully in the background. Let's check if the device is now visible to adb:

```bash
adb devices
```

If you see a device listed, you can now install and run your app with:

```bash
# Install via Gradle
./gradlew installDebug

# Or install via adb directly
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Then launch the app
adb shell am start -n com.example.androidkt/.MainActivity
```

If adb still doesn't show any devices, you can try:

1. Restart the adb server:
   ```bash
   adb kill-server
   adb start-server
   ```

2. Try connecting explicitly:
   ```bash
   adb connect localhost:5554
   ```

If you continue to have issues with the emulator, you have a working APK file at `app/build/outputs/apk/debug/app-debug.apk` that you can transfer to a physical Android device for testing.

You can transfer the APK to your device using:
- USB connection and manual installation
- Email attachment
- Cloud storage (Google Drive, Dropbox, etc.)
- ADB if you have a physical device connected

Once on your Android device, you'll need to enable "Install from Unknown Sources" in your security settings, then open the file manager and tap on the APK to install it.