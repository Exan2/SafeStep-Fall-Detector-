package com.falldetector.safestep;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class EmergencyAlertActivity extends AppCompatActivity {
    
    private static final int TAP_COUNT_REQUIRED = 3;
    private static final int COUNTDOWN_DURATION = 10000; // 10 seconds
    
    private TextView tvCountdown;
    private TextView tvTapInstruction;
    private ImageView ivPulse;
    private int tapCount = 0;
    private CountDownTimer countDownTimer;
    private FusedLocationProviderClient fusedLocationClient;
    private Vibrator vibrator;
    private Animation shimmerAnim;
    private ObjectAnimator pulseAnimator;
    private static final String TAG = "EmergencyAlertActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configure window to show over lock screen
        configureWindowForLockScreen();
        
        setContentView(R.layout.activity_emergency_alert);
        
        initViews();
        initServices();
        startCountdown();
        startVibration();
        startPulseViewAnimation();
    }
    
    private void configureWindowForLockScreen() {
        try {
            // Show over lock screen and keep screen on
            getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        } catch (Exception e) {
            // Fallback minimal flags
            getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            );
        }
    }
    
    private void initViews() {
        tvCountdown = findViewById(R.id.tv_countdown);
        tvTapInstruction = findViewById(R.id.tv_tap_instruction);
        ivPulse = findViewById(R.id.iv_pulse);
        
        // Make the entire screen tappable
        findViewById(R.id.root_layout).setOnClickListener(this::handleScreenTap);
        
        // Prepare shimmer-like animation for countdown
        shimmerAnim = new AlphaAnimation(0.4f, 1.0f);
        shimmerAnim.setDuration(600);
        shimmerAnim.setRepeatMode(Animation.REVERSE);
        shimmerAnim.setRepeatCount(Animation.INFINITE);
        tvCountdown.startAnimation(shimmerAnim);
    }
    
    private void initServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }
    
    private void startPulseViewAnimation() {
        if (ivPulse == null) return;
        TimeInterpolator ease = new AccelerateDecelerateInterpolator();
        PropertyValuesHolder sx = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.3f, 2.2f);
        PropertyValuesHolder sy = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.3f, 2.2f);
        PropertyValuesHolder a = PropertyValuesHolder.ofFloat(View.ALPHA, 0.95f, 0.05f);
        pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(ivPulse, sx, sy, a);
        pulseAnimator.setDuration(2000);
        pulseAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        pulseAnimator.setInterpolator(ease);
        pulseAnimator.start();
    }
    
    private void startCountdown() {
        countDownTimer = new CountDownTimer(COUNTDOWN_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                tvCountdown.setText(String.valueOf(seconds));
            }
            
            @Override
            public void onFinish() {
                // Time's up - send emergency SMS
                stopVibration();
                stopAnimations();
                sendEmergencySMS();
            }
        };
        countDownTimer.start();
    }
    
    private void startVibration() {
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 500, 200, 500, 200, 500};
            vibrator.vibrate(pattern, 0);
        }
    }
    
    private void stopVibration() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }
    
    private void handleScreenTap(View view) {
        // Subtle haptic feedback on each tap
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(40);
            }
        }
        
        tapCount++;
        
        if (tapCount >= TAP_COUNT_REQUIRED) {
            cancelEmergency();
        } else {
            int remaining = TAP_COUNT_REQUIRED - tapCount;
            tvTapInstruction.setText("Tap " + remaining + " more times to confirm you're okay");
        }
    }
    
    private void cancelEmergency() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        stopVibration();
        stopAnimations();
        android.util.Log.d(TAG, "Emergency cancelled by user");
        finish();
    }
    
    private void stopAnimations() {
        if (tvCountdown != null) tvCountdown.clearAnimation();
        if (pulseAnimator != null) pulseAnimator.cancel();
    }
    
    private void sendEmergencySMS() {
        android.util.Log.d(TAG, "EMERGENCY TRIGGERED - Sending SMS to contacts...");
        
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            android.util.Log.d(TAG, "Location permission granted, getting location...");
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            String locationText = String.format("Location: %.6f, %.6f", 
                                location.getLatitude(), location.getLongitude());
                            android.util.Log.d(TAG, "Location found: " + locationText);
                            sendLocationSMS(location);
                        } else {
                            android.util.Log.d(TAG, "Location is null, sending without coordinates");
                            sendLocationSMS(null);
                        }
                        // Only send SMS, no emergency call
                        finish();
                    }
                });
        } else {
            android.util.Log.d(TAG, "Location permission not granted, sending without coordinates");
            // Send SMS without location if permission not granted
            sendLocationSMS(null);
            finish();
        }
    }
    
    
    private void sendLocationSMS(Location location) {
        // Check SMS permission first
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
            return;
        }
        
        // Load emergency contacts
        List<EmergencyContact> contacts = loadEmergencyContacts();
        android.util.Log.d(TAG, "Contacts loaded: " + contacts.size());
        
        if (contacts.isEmpty()) {
            Toast.makeText(this, getString(R.string.emergency_contacts), Toast.LENGTH_LONG).show();
            return;
        }
        
        // Get user info for the message
        SharedPreferences userPrefs = getSharedPreferences("UserInfoPrefs", MODE_PRIVATE);
        String userName = userPrefs.getString("name", "User");
        String userAge = userPrefs.getString("age", "");
        String userBloodType = userPrefs.getString("blood_type", "");
        String userWilaya = userPrefs.getString("wilaya", "");
        String userDaira = userPrefs.getString("daira", "");
        
        String locationText = "Location: Unknown";
        if (location != null) {
            locationText = String.format("Location: %.6f, %.6f", 
                location.getLatitude(), location.getLongitude());
        }
        
        String message = "🚨 EMERGENCY ALERT - SStep App\n\n" +
                        "URGENT: " + userName + " has fallen and needs immediate help!\n\n" +
                        "Personal Info:\n" +
                        "• Name: " + userName + "\n" +
                        "• Age: " + userAge + "\n" +
                        "• Blood Type: " + userBloodType + "\n" +
                        "• Location: " + userWilaya + ", " + userDaira + "\n" +
                        "• Coordinates: " + locationText + "\n\n" +
                        "Please respond immediately and contact emergency services if needed.";
        
        android.util.Log.d(TAG, "Sending emergency SMS to " + contacts.size() + ", length=" + message.length());
        
        SmsManager smsManager = SmsManager.getDefault();
        int successCount = 0;
        
        for (EmergencyContact contact : contacts) {
            try {
                String originalPhone = contact.getPhone();
                String phoneNumber = formatAlgerianPhoneNumber(contact.getPhone());
                android.util.Log.d(TAG, "Contact=" + contact.getName() + ", original=" + originalPhone + ", formatted=" + phoneNumber);
                
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                successCount++;
                android.util.Log.d(TAG, "SMS sent to " + contact.getName());
            } catch (Exception e) {
                android.util.Log.d(TAG, "SmsManager failed for " + contact.getName() + ": " + e.getMessage());
                try {
                    android.util.Log.d(TAG, "Trying alternative SMS for " + contact.getName());
                    tryAlternativeSMS(contact, message);
                    successCount++;
                    android.util.Log.d(TAG, "Alternative SMS sent to " + contact.getName());
                } catch (Exception altException) {
                    android.util.Log.d(TAG, "Alternative SMS failed for " + contact.getName() + ": " + altException.getMessage());
                    altException.printStackTrace();
                }
                e.printStackTrace();
            }
        }
        // Optional success feedback suppressed; rely on logs
    }
    
    private List<EmergencyContact> loadEmergencyContacts() {
        SharedPreferences prefs = getSharedPreferences("EmergencyContactsPrefs", MODE_PRIVATE);
        String contactsJson = prefs.getString("emergency_contacts", "");
        
        if (!contactsJson.isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<EmergencyContact>>(){}.getType();
            return gson.fromJson(contactsJson, type);
        }
        
        return new java.util.ArrayList<>();
    }
    
    private String formatAlgerianPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return phoneNumber;
        }
        
        // Remove all spaces, dashes, and parentheses
        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");
        
        // If it starts with 0, replace with +213
        if (cleaned.startsWith("0")) {
            return "+213" + cleaned.substring(1);
        }
        
        // If it starts with 213, add +
        if (cleaned.startsWith("213")) {
            return "+" + cleaned;
        }
        
        // If it already has +213, return as is
        if (cleaned.startsWith("+213")) {
            return cleaned;
        }
        
        // If it's a 9-digit number, assume it's Algerian and add +213
        if (cleaned.length() == 9 && cleaned.matches("\\d{9}")) {
            return "+213" + cleaned;
        }
        
        // Return as is if we can't determine the format
        return phoneNumber;
    }
    
    private void tryAlternativeSMS(EmergencyContact contact, String message) {
        try {
            String phoneNumber = formatAlgerianPhoneNumber(contact.getPhone());
            Uri smsUri = Uri.parse("smsto:" + phoneNumber);
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO, smsUri);
            smsIntent.putExtra("sms_body", message);
            smsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(smsIntent);
            android.util.Log.d(TAG, "Opened SMS app for " + contact.getName());
        } catch (Exception e) {
            android.util.Log.d(TAG, "Alternative SMS failed: " + e.getMessage());
            throw e;
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        stopVibration();
        stopAnimations();
    }
    
    @Override
    public void onBackPressed() {
        // Prevent back button from cancelling emergency
    }
}

