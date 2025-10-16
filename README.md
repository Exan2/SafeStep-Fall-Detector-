SafeStep – Fall Detector (Android)

Reliable fall detection with emergency SMS to selected contacts (including GPS coordinates), a lock-screen emergency alert, and a modern, accessible UI.

Features
- Real-time fall detection service (foreground) using accelerometer and gyroscope with tuned sensitivity
- Emergency alert screen that wakes and shows over the lock screen
- Automatic SMS to emergency contacts with GPS coordinates (multipart-safe)
- Robust location acquisition: current, last-known, single-update with timeouts and fallbacks
- Emergency Contacts manager (add/edit/delete) stored locally on-device
- Personal info screen (name, age, blood type, wilaya, daira) stored locally on-device
- Arabic/English language toggle on home screen
- Material 3 UI with modern visual design

Privacy
- No cloud backend: contacts and personal information are stored only on the user’s device via SharedPreferences.
- Location is obtained at send time to include coordinates in the outgoing SMS message; it is not uploaded anywhere by the app.
- The project repository contains no personal data or secrets.

Permissions
- ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION: Fetch GPS coordinates for emergency SMS
- SEND_SMS: Send emergency/test SMS to selected contacts
- VIBRATE, WAKE_LOCK, FOREGROUND_SERVICE: Keep service active, haptics, and wake screen for emergencies
- INTERNET/ACCESS_NETWORK_STATE: Improve location acquisition (when network is used by fused provider)

Build & Run
1) Open the project in Android Studio (Giraffe or newer recommended)
2) Let Gradle sync the project
3) Build > Make Project, or Build > Build APK(s)
4) Run on a device (recommended for sensors/SMS testing); grant requested permissions

Testing
- Settings > Emergency Contacts: add a test contact (use your own secondary phone)
- From the contacts screen, use the Test action to verify SMS delivery and location inclusion
- From the home screen, start monitoring and simulate a fall to see the emergency alert

Arabic Language
- On the home screen, tap “Switch to Arabic” to translate UI texts to Arabic instantly; tap again to switch back.

Known Notes
- SMS delivery depends on carrier/device policies. Some devices may restrict silent SMS.
- Location fetching quality depends on device settings and environment; the app uses multiple fallbacks and short timeouts.

License
This project is provided for personal/educational use. Add a license of your choice if you plan to distribute.

# SafeStep - Fall Detection App

A comprehensive Android application designed to detect falls in elderly users and automatically trigger emergency responses when needed.

## Features

### 🚨 Fall Detection
- **Smart Sensor Monitoring**: Uses accelerometer and gyroscope to detect sudden impacts and unusual movement patterns
- **Configurable Sensitivity**: Adjustable fall detection sensitivity levels (1-5)
- **Real-time Monitoring**: Continuous background monitoring with minimal battery impact

### 🆘 Emergency Response
- **3-Tap Confirmation**: Users can cancel emergency alerts by tapping the screen 3 times
- **Countdown Timer**: Configurable countdown (5-30 seconds) before emergency call
- **Automatic Emergency Call**: Calls emergency services if user doesn't respond
- **Location Sharing**: Sends GPS coordinates to emergency contacts via SMS

### 📱 User Interface
- **Onboarding Flow**: 3-screen introduction explaining app features
- **Personal Information**: Secure storage of user details for emergency responders
- **Settings Management**: Easy configuration of detection sensitivity and countdown duration
- **Emergency Alert Screen**: Clear, accessible interface for emergency situations

### 🔒 Privacy & Security
- **Local Data Storage**: Personal information stored securely on device
- **Permission-based Sharing**: Location only shared during actual emergencies
- **No Data Collection**: App doesn't collect or transmit personal data unnecessarily

## Screenshots

The app includes 6 main screens:
1. **Onboarding Screen 1**: Welcome screen with falling man illustration
2. **Onboarding Screen 2**: Features overview with location pin illustration  
3. **Onboarding Screen 3**: Technical explanation with smartphone illustration
4. **Emergency Alert Screen**: Fall detection alert with countdown timer
5. **User Information Screen**: Personal details form for emergency responders
6. **Settings Screen**: Configuration options for fall detection

## Technical Implementation

### Architecture
- **Language**: Java
- **Target SDK**: Android 14 (API 34)
- **Minimum SDK**: Android 7.0 (API 24)
- **Architecture**: Service-based fall detection with Activity-based UI

### Key Components
- `MainActivity`: Home screen and app entry point
- `OnboardingActivity`: 3-screen introduction flow
- `EmergencyAlertActivity`: Emergency response interface
- `UserInfoActivity`: Personal information management
- `SettingsActivity`: App configuration
- `FallDetectionService`: Background sensor monitoring
- `OnboardingAdapter`: ViewPager adapter for onboarding screens

### Fall Detection Algorithm
1. **Accelerometer Monitoring**: Detects sudden impacts and free-fall conditions
2. **Gyroscope Analysis**: Additional motion pattern recognition
3. **Threshold-based Detection**: Configurable sensitivity levels
4. **Time-window Validation**: Prevents false positives from brief movements
5. **Emergency Trigger**: Activates alert system when fall is confirmed

### Permissions Required
- `ACCESS_FINE_LOCATION`: For GPS coordinates during emergencies
- `CALL_PHONE`: For emergency calls
- `SEND_SMS`: For location sharing with contacts
- `VIBRATE`: For haptic feedback during alerts
- `WAKE_LOCK`: For background monitoring

## Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24 or higher
- Java 8 or later

### Installation
1. Clone or download the project
2. Open Android Studio
3. Import the project from the root directory
4. Sync Gradle files
5. Build and run on device or emulator

### Adding Illustrations
1. Place your 3 onboarding illustrations in `app/src/main/res/drawable/illustrations/`
2. Name them:
   - `onboarding_1_falling_man.png` (or .jpg)
   - `onboarding_2_location_pin.png` (or .jpg)  
   - `onboarding_3_smartphone.png` (or .jpg)
3. Update the `ILLUSTRATIONS` array in `OnboardingAdapter.java` to reference your images

### Configuration
1. **Emergency Number**: Update the emergency number in `EmergencyAlertActivity.java` (currently set to 911)
2. **Emergency Contacts**: Implement contact management in `SettingsActivity.java`
3. **Sensitivity Tuning**: Adjust fall detection thresholds in `FallDetectionService.java`

## Usage

### First Launch
1. Complete the 3-screen onboarding process
2. Grant necessary permissions (location, phone, SMS)
3. Fill in personal information for emergency responders
4. Configure fall detection settings

### Daily Use
1. Enable fall detection in settings
2. Keep the app running in background
3. The service will monitor for falls automatically
4. If a fall is detected, follow the on-screen instructions

### Emergency Response
1. When fall is detected, emergency screen appears
2. Tap screen 3 times to cancel if you're okay
3. If no response, emergency call will be made automatically
4. Location will be sent to emergency contacts

## Customization

### Fall Detection Sensitivity
- Adjust `FALL_THRESHOLD` and `IMPACT_THRESHOLD` in `FallDetectionService.java`
- Modify sensitivity levels in `SettingsActivity.java`

### UI Customization
- Colors defined in `res/values/colors.xml`
- Styles in `res/values/themes.xml`
- Layouts in `res/layout/` directory

### Emergency Response
- Modify countdown duration in `EmergencyAlertActivity.java`
- Update emergency number and contact logic
- Customize SMS message format

## Testing

### Manual Testing
1. Use the "Test Fall Detection" button in settings
2. Simulate falls by shaking device vigorously
3. Test emergency response flow
4. Verify permission handling

### Device Testing
- Test on various Android versions (7.0+)
- Verify sensor availability on different devices
- Test battery impact during extended monitoring

## Future Enhancements

- Machine learning-based fall detection
- Integration with wearable devices
- Cloud backup of emergency contacts
- Multi-language support
- Voice commands for emergency cancellation
- Integration with smart home systems

## Support

For technical support or feature requests, please refer to the project documentation or contact the development team.

## License

This project is developed for educational and personal use. Please ensure compliance with local regulations regarding emergency services and medical device requirements.
