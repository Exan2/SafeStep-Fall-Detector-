package com.falldetector.safestep;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    
    private static final String PREFS_NAME = "SettingsPrefs";
    
    // Personal Info Display
    private TextView tvUserName;
    private TextView tvUserAge;
    private TextView tvUserBloodType;
    private TextView tvUserWilaya;
    private TextView tvUserDaira;
    private TextView tvUserPhone;
    
    // Detector Settings
    private Switch switchFallDetection;
    private TextView tvDetectorStatus;
    private SeekBar seekBarSensitivity;
    private SeekBar seekBarCountdown;
    private TextView tvSensitivityValue;
    private TextView tvCountdownValue;
    
    // Action Buttons
    private Button btnEditPersonalInfo;
    private Button btnEmergencyContacts;
    private Button btnTestFallDetection;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        initViews();
        loadUserInfo();
        loadSettings();
    }
    
    private <T extends View> T safeFind(int id) {
        try {
            return findViewById(id);
        } catch (Exception ignored) {
            return null;
        }
    }
    
    private void initViews() {
        // Personal Info Display
        tvUserName = safeFind(R.id.tv_user_name);
        tvUserAge = safeFind(R.id.tv_user_age);
        tvUserBloodType = safeFind(R.id.tv_user_blood_type);
        tvUserWilaya = safeFind(R.id.tv_user_wilaya);
        tvUserDaira = safeFind(R.id.tv_user_daira);
        tvUserPhone = safeFind(R.id.tv_user_phone);
        
        // Detector Settings
        switchFallDetection = safeFind(R.id.switch_fall_detection);
        tvDetectorStatus = safeFind(R.id.tv_detector_status);
        seekBarSensitivity = safeFind(R.id.seekbar_sensitivity);
        seekBarCountdown = safeFind(R.id.seekbar_countdown);
        tvSensitivityValue = safeFind(R.id.tv_sensitivity_value);
        tvCountdownValue = safeFind(R.id.tv_countdown_value);
        
        // Action Buttons
        btnEditPersonalInfo = safeFind(R.id.btn_edit_personal_info);
        btnEmergencyContacts = safeFind(R.id.btn_emergency_contacts);
        btnTestFallDetection = safeFind(R.id.btn_test_fall_detection);
        
        // Set up listeners (only if views exist)
        if (switchFallDetection != null) {
            switchFallDetection.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveSettings();
                if (isChecked) {
                    startFallDetectionService();
                } else {
                    stopFallDetectionService();
                }
                updateDetectorStatus(isChecked);
            });
        }
        
        if (seekBarSensitivity != null) {
            seekBarSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (tvSensitivityValue != null) tvSensitivityValue.setText(String.valueOf(progress + 1));
                    saveSettings();
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }
        
        if (seekBarCountdown != null) {
            seekBarCountdown.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int seconds = (progress + 1) * 5; // 5, 10, 15, 20, 25, 30 seconds
                    if (tvCountdownValue != null) tvCountdownValue.setText(seconds + " seconds");
                    saveSettings();
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }
        
        if (btnEditPersonalInfo != null) {
            btnEditPersonalInfo.setOnClickListener(v -> startActivity(new Intent(this, UserInfoActivity.class)));
        }
        if (btnEmergencyContacts != null) {
            btnEmergencyContacts.setOnClickListener(v -> openEmergencyContacts());
        }
        if (btnTestFallDetection != null) {
            btnTestFallDetection.setOnClickListener(v -> testFallDetection());
        }
    }
    
    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("UserInfoPrefs", MODE_PRIVATE);
        
        String name = prefs.getString("name", "Not set");
        String dob = prefs.getString("dob", "");
        String bloodType = prefs.getString("blood_type", "Not set");
        String wilaya = prefs.getString("wilaya", "Not set");
        String daira = prefs.getString("daira", "Not set");
        String phone = prefs.getString("phone", "Not set");
        
        if (tvUserName != null) tvUserName.setText(name);
        if (tvUserBloodType != null) tvUserBloodType.setText(bloodType);
        if (tvUserWilaya != null) tvUserWilaya.setText(wilaya);
        if (tvUserDaira != null) tvUserDaira.setText(daira);
        if (tvUserPhone != null) tvUserPhone.setText(phone);
        
        // Calculate age from date of birth
        if (tvUserAge != null) {
            if (!dob.isEmpty()) {
                int age = calculateAge(dob);
                tvUserAge.setText(age + " years old");
            } else {
                tvUserAge.setText("Not set");
            }
        }
    }
    
    private int calculateAge(String dob) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date birthDate = sdf.parse(dob);
            Calendar birth = Calendar.getInstance();
            birth.setTime(birthDate);
            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return age;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean fallDetectionEnabled = prefs.getBoolean("fall_detection_enabled", true);
        int sensitivity = prefs.getInt("sensitivity", 2); // Default to 3 (0-4 scale)
        int countdown = prefs.getInt("countdown", 1); // Default to 10 seconds (1 = 10s)
        
        if (switchFallDetection != null) switchFallDetection.setChecked(fallDetectionEnabled);
        if (seekBarSensitivity != null) seekBarSensitivity.setProgress(sensitivity);
        if (seekBarCountdown != null) seekBarCountdown.setProgress(countdown);
        
        if (tvSensitivityValue != null) tvSensitivityValue.setText(String.valueOf(sensitivity + 1));
        int countdownSeconds = (countdown + 1) * 5;
        if (tvCountdownValue != null) tvCountdownValue.setText(countdownSeconds + " seconds");
        
        updateDetectorStatus(fallDetectionEnabled);
    }
    
    private void saveSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        if (switchFallDetection != null)
            editor.putBoolean("fall_detection_enabled", switchFallDetection.isChecked());
        if (seekBarSensitivity != null)
            editor.putInt("sensitivity", seekBarSensitivity.getProgress());
        if (seekBarCountdown != null)
            editor.putInt("countdown", seekBarCountdown.getProgress());
        
        editor.apply();
    }
    
    private void updateDetectorStatus(boolean enabled) {
        if (tvDetectorStatus == null) return;
        if (enabled) {
            tvDetectorStatus.setText("ON");
            tvDetectorStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvDetectorStatus.setText("OFF");
            tvDetectorStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadUserInfo();
    }
    
    private void startFallDetectionService() {
        Intent serviceIntent = new Intent(this, FallDetectionService.class);
        startService(serviceIntent);
    }
    
    private void stopFallDetectionService() {
        Intent serviceIntent = new Intent(this, FallDetectionService.class);
        stopService(serviceIntent);
    }
    
    private void openEmergencyContacts() {
        Intent intent = new Intent(this, EmergencyContactsActivity.class);
        startActivity(intent);
    }
    
    private void testFallDetection() {
        Intent testIntent = new Intent(this, EmergencyAlertActivity.class);
        startActivity(testIntent);
    }
}
