@echo off
echo ========================================
echo    SafeStep Fall Detection App Test
echo ========================================
echo.

echo Checking for connected Android devices...
adb devices
echo.

echo If you see your device listed above, we can proceed.
echo If not, please:
echo 1. Enable Developer Options on your Android device
echo 2. Enable USB Debugging
echo 3. Connect your device via USB
echo 4. Allow USB debugging when prompted
echo.

pause

echo.
echo Building the app...
call gradlew.bat assembleDebug
if %ERRORLEVEL% neq 0 (
    echo Build failed! Please check the error messages above.
    pause
    exit /b 1
)

echo.
echo Build successful! Installing on device...
adb install -r app\build\outputs\apk\debug\app-debug.apk
if %ERRORLEVEL% neq 0 (
    echo Installation failed! Please check:
    echo 1. Device is connected and authorized
    echo 2. USB debugging is enabled
    echo 3. Device has enough storage space
    pause
    exit /b 1
)

echo.
echo ========================================
echo    Installation Complete!
echo ========================================
echo.
echo The SafeStep app has been installed on your device.
echo You can now:
echo 1. Open the app from your device's app drawer
echo 2. Test the onboarding flow
echo 3. Test the emergency alert screen
echo 4. Configure settings
echo.
echo To test fall detection:
echo 1. Go to Settings
echo 2. Enable fall detection
echo 3. Use "Test Fall Detection" button
echo.
pause
