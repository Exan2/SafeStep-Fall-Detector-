# 🔧 Build Fix Instructions

## Issue Fixed
✅ **XML Namespace Error**: Fixed missing `app` namespace in `activity_onboarding.xml`

## The Problem
The build was failing because the `TabLayout` in the onboarding layout was using `app:` attributes without declaring the `app` namespace.

## What I Fixed
Added the missing namespace declaration:
```xml
xmlns:app="http://schemas.android.com/apk/res-auto"
```

## How to Build Now

### Option 1: Use Android Studio (Recommended)
1. **Open Android Studio**
2. **Open Project** → Navigate to `C:\Users\compt\OneDrive\Bureau\Fall Detector`
3. **Wait for Gradle sync** to complete
4. **Click Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
5. **Or click Run** (green play button) to build and install directly

### Option 2: Use Command Line (If Gradle is properly set up)
```cmd
cd "C:\Users\compt\OneDrive\Bureau\Fall Detector"
gradlew.bat assembleDebug
```

### Option 3: Download Gradle Wrapper JAR
If you want to use command line, download the Gradle wrapper JAR:
1. Go to: https://github.com/gradle/gradle/raw/v8.0.0/gradle/wrapper/gradle-wrapper.jar
2. Save as: `gradle\wrapper\gradle-wrapper.jar`
3. Then run: `gradlew.bat assembleDebug`

## Expected Result
The build should now complete successfully and create:
- `app\build\outputs\apk\debug\app-debug.apk`

## Next Steps After Build
1. **Install on device**: `adb install -r app\build\outputs\apk\debug\app-debug.apk`
2. **Or use Android Studio**: Click Run button to install automatically
3. **Test the app** following the testing guide

## Files Fixed
- ✅ `app/src/main/res/layout/activity_onboarding.xml` - Added missing namespace

The app should now build successfully! 🎉
