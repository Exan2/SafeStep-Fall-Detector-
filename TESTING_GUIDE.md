# 🧪 SafeStep App Testing Guide

## Prerequisites

### 1. Enable Developer Options on Your Android Device
1. **Go to Settings** → **About Phone**
2. **Tap "Build Number"** 7 times rapidly
3. **Go back to Settings** → **Developer Options**
4. **Enable "USB Debugging"**
5. **Enable "Install via USB"** (if available)

### 2. Connect Your Device
1. **Connect via USB cable**
2. **Allow USB debugging** when prompted on device
3. **Verify connection** by running `adb devices` in command prompt

## Quick Test (Automated)

### Option A: Use the Test Script
1. **Double-click** `test_app.bat`
2. **Follow the prompts** on screen
3. **Wait for installation** to complete

### Option B: Manual Build
1. **Open Command Prompt** in project folder
2. **Run**: `gradlew.bat assembleDebug`
3. **Install**: `adb install -r app\build\outputs\apk\debug\app-debug.apk`

## Manual Testing Checklist

### ✅ Onboarding Flow
- [ ] **Screen 1**: Welcome screen with falling man illustration
- [ ] **Screen 2**: Features overview with location pin illustration
- [ ] **Screen 3**: Technical explanation with smartphone illustration
- [ ] **Navigation**: Next/Skip buttons work correctly
- [ ] **Completion**: Leads to main screen after onboarding

### ✅ Main Screen
- [ ] **Start Monitoring**: Button starts fall detection service
- [ ] **Update Personal Info**: Opens user information form
- [ ] **Settings**: Opens settings screen
- [ ] **UI Elements**: All buttons and text display correctly

### ✅ User Information Form
- [ ] **Form Fields**: Name, DOB, ID, Blood Type, Location, Phone
- [ ] **Validation**: Required fields (Name, Phone) are validated
- [ ] **Save Function**: Data saves and returns to main screen
- [ ] **Input Types**: Correct keyboard types for each field

### ✅ Settings Screen
- [ ] **Fall Detection Toggle**: Can enable/disable monitoring
- [ ] **Sensitivity Slider**: Adjusts from 1-5 levels
- [ ] **Countdown Slider**: Adjusts from 5-30 seconds
- [ ] **Emergency Contacts**: Button works (placeholder)
- [ ] **Test Fall Detection**: Opens emergency alert screen

### ✅ Emergency Alert Screen
- [ ] **Visual Design**: Red background with white text
- [ ] **Instructions**: Clear 3-tap instruction text
- [ ] **Countdown Timer**: Shows decreasing seconds
- [ ] **3-Tap Cancel**: Tapping 3 times cancels emergency
- [ ] **Vibration**: Device vibrates during alert
- [ ] **Emergency Call**: Makes call after countdown (if permissions granted)

### ✅ Fall Detection Service
- [ ] **Background Monitoring**: Runs when enabled
- [ ] **Notification**: Shows persistent notification
- [ ] **Sensor Access**: Uses accelerometer and gyroscope
- [ ] **Battery Impact**: Minimal battery usage
- [ ] **Service Restart**: Restarts after emergency alert

## Permission Testing

### Required Permissions
- [ ] **Location**: Grant when prompted
- [ ] **Phone**: Grant when prompted  
- [ ] **SMS**: Grant when prompted
- [ ] **Vibration**: Usually granted automatically

### Permission Denial Testing
- [ ] **Location Denied**: App still works, no location sharing
- [ ] **Phone Denied**: Emergency call fails gracefully
- [ ] **SMS Denied**: Location SMS fails gracefully

## Advanced Testing

### Fall Detection Simulation
1. **Enable fall detection** in settings
2. **Shake device vigorously** to simulate fall
3. **Verify emergency alert** appears
4. **Test 3-tap cancellation**
5. **Test emergency call** (be ready to cancel quickly!)

### Battery and Performance
- [ ] **Battery Usage**: Check device battery settings
- [ ] **Memory Usage**: Monitor RAM usage
- [ ] **Background Activity**: Verify service runs continuously
- [ ] **App Stability**: No crashes during extended use

### UI/UX Testing
- [ ] **Screen Rotation**: Test in both orientations
- [ ] **Different Screen Sizes**: Test on various devices
- [ ] **Accessibility**: Large text, high contrast
- [ ] **Touch Targets**: All buttons easy to tap

## Troubleshooting

### Build Issues
- **Gradle Error**: Check Java version (8+ required)
- **SDK Error**: Install Android SDK 24+
- **Permission Error**: Run as administrator

### Installation Issues
- **Device Not Found**: Check USB debugging
- **Installation Failed**: Check device storage
- **App Crashes**: Check logcat for errors

### Runtime Issues
- **Sensors Not Working**: Check device compatibility
- **Emergency Call Fails**: Check phone permissions
- **Location Not Shared**: Check location permissions

## Test Results Log

| Test Category | Status | Notes |
|---------------|--------|-------|
| Onboarding Flow | ⬜ | |
| Main Screen | ⬜ | |
| User Info Form | ⬜ | |
| Settings | ⬜ | |
| Emergency Alert | ⬜ | |
| Fall Detection | ⬜ | |
| Permissions | ⬜ | |
| Performance | ⬜ | |

## Success Criteria
- ✅ App builds and installs successfully
- ✅ All screens display correctly
- ✅ Fall detection service runs in background
- ✅ Emergency alert system works
- ✅ User can configure all settings
- ✅ No crashes during normal use

## Next Steps After Testing
1. **Report any issues** found during testing
2. **Replace placeholder illustrations** with your designs
3. **Customize emergency number** for your region
4. **Add emergency contacts** functionality
5. **Fine-tune fall detection** sensitivity

---

**Ready to test?** Run `test_app.bat` or follow the manual build instructions above!
