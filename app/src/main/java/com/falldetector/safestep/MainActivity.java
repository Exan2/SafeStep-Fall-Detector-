package com.falldetector.safestep;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final String PREFS_NAME = "SafeStepPrefs";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_LOCALE = "app_locale";
    
    private Button btnStartMonitoring;
    private Button btnSettings;
    private Button btnUserInfo;
    private Button btnLanguage;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applySavedLocale();
        setContentView(R.layout.activity_main);
        
        initViews();
        checkFirstLaunch();
        checkPermissions();
    }
    
    private void initViews() {
        btnStartMonitoring = findViewById(R.id.btn_start_monitoring);
        btnSettings = findViewById(R.id.btn_settings);
        btnUserInfo = findViewById(R.id.btn_user_info);
        btnLanguage = findViewById(R.id.btn_language);
        
        btnStartMonitoring.setOnClickListener(v -> startFallDetection());
        btnSettings.setOnClickListener(v -> openSettings());
        btnUserInfo.setOnClickListener(v -> openUserInfo());
        btnLanguage.setOnClickListener(v -> toggleLanguage());
    }
    
    private void checkFirstLaunch() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true);
        
        if (isFirstLaunch) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
        }
    }
    
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this, 
                new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.SEND_SMS
                }, 
                PERMISSION_REQUEST_CODE);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            
            if (!allPermissionsGranted) {
                Toast.makeText(this, R.string.permission_denied_message, Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void startFallDetection() {
        Intent serviceIntent = new Intent(this, FallDetectionService.class);
        startService(serviceIntent);
        
        Toast.makeText(this, "Fall detection started", Toast.LENGTH_SHORT).show();
    }
    
    private void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }
    
    private void openUserInfo() {
        startActivity(new Intent(this, UserInfoActivity.class));
    }
    
    private void toggleLanguage() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String current = prefs.getString(KEY_LOCALE, "en");
        String next = current.equals("ar") ? "en" : "ar";
        
        setLocale(next);
        prefs.edit().putString(KEY_LOCALE, next).apply();
        
        recreate();
    }
    
    private void applySavedLocale() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String locale = prefs.getString(KEY_LOCALE, "en");
        setLocale(locale);
    }
    
    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration(getResources().getConfiguration());
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}
