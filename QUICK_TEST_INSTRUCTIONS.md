# 🚀 Quick Test Instructions for SafeStep App

## Method 1: Using Android Studio (Recommended)

### Step 1: Open Project in Android Studio
1. **Launch Android Studio**
2. **Click "Open an existing project"**
3. **Navigate to**: `C:\Users\compt\OneDrive\Bureau\Fall Detector`
4. **Select the project folder** and click "OK"
5. **Wait for Gradle sync** to complete

### Step 2: Connect Your Android Device
1. **Enable Developer Options** on your Android device:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Go back to Settings → Developer Options
   - Enable "USB Debugging"
2. **Connect device via USB**
3. **Allow USB debugging** when prompted on device

### Step 3: Build and Install
1. **In Android Studio**, click the "Run" button (green play icon)
2. **Select your connected device** from the dropdown
3. **Wait for build and installation** to complete
4. **App will launch automatically** on your device

## Method 2: Using Command Line (Alternative)

### Prerequisites
- Install Android SDK
- Add `platform-tools` to your PATH
- Install Java 8 or later

### Steps
1. **Open Command Prompt** as Administrator
2. **Navigate to project folder**:
   ```cmd
   cd "C:\Users\compt\OneDrive\Bureau\Fall Detector"
   ```
3. **Build the app**:
   ```cmd
   gradlew.bat assembleDebug
   ```
4. **Install on device**:
   ```cmd
   adb install -r app\build\outputs\apk\debug\app-debug.apk
   ```

## What to Test

### ✅ Onboarding Flow
1. **First Launch**: Should show 3 onboarding screens
2. **Navigation**: Use Next/Skip buttons
3. **Illustrations**: Check if placeholder images display
4. **Completion**: Should lead to main screen

### ✅ Main Features
1. **Start Monitoring**: Tap to enable fall detection
2. **Personal Info**: Fill out the form with your details
3. **Settings**: Configure sensitivity and countdown
4. **Test Emergency**: Use "Test Fall Detection" button

### ✅ Emergency Alert
1. **Visual**: Red screen with countdown timer
2. **3-Tap Cancel**: Tap screen 3 times to cancel
3. **Vibration**: Device should vibrate
4. **Emergency Call**: Will call 911 after countdown

## Expected Behavior

### First Launch
- Shows onboarding screens
- Requests permissions (Location, Phone, SMS)
- Leads to main screen

### Normal Operation
- Background service monitors for falls
- Persistent notification shows "SafeStep is monitoring"
- Settings allow configuration

### Emergency Response
- Red alert screen appears
- 10-second countdown timer
- 3-tap cancellation works
- Emergency call if no response

## Troubleshooting

### Build Issues
- **Gradle Error**: Update Android Studio
- **SDK Error**: Install Android SDK 24+
- **Java Error**: Install Java 8+

### Installation Issues
- **Device Not Found**: Check USB debugging
- **Permission Denied**: Run as administrator
- **Storage Full**: Free up device space

### Runtime Issues
- **App Crashes**: Check logcat in Android Studio
- **Sensors Not Working**: Test on different device
- **Emergency Call Fails**: Check phone permissions

## Success Indicators
- ✅ App builds without errors
- ✅ Installs on device successfully
- ✅ Onboarding flow works
- ✅ All screens display correctly
- ✅ Fall detection service starts
- ✅ Emergency alert system works
- ✅ Settings can be configured

## Next Steps After Testing
1. **Report any issues** you encounter
2. **Test on different devices** if possible
3. **Replace placeholder illustrations** with your designs
4. **Customize for your specific needs**

---

**Ready to test?** Use Method 1 (Android Studio) for the easiest experience!
