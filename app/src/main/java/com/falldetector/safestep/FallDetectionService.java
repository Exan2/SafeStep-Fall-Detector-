package com.falldetector.safestep;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;

public class FallDetectionService extends Service implements SensorEventListener {
    
    private static final String TAG = "FallDetectionService";
    private static final String CHANNEL_ID = "FallDetectionChannel";
    private static final int NOTIFICATION_ID = 1;
    
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Vibrator vibrator;
    
    // Fall detection parameters - Made less sensitive for real falls only
    private static final float FALL_THRESHOLD = 25.0f; // m/s² (increased from 15.0f - less sensitive)
    private static final float IMPACT_THRESHOLD = 20.0f; // m/s² (increased from 12.0f - less sensitive)
    private static final long FALL_DETECTION_WINDOW = 1000; // 1 second
    private static final long IMPACT_DETECTION_WINDOW = 500; // 0.5 seconds
    
    private float[] lastAcceleration = new float[3];
    private float[] lastGyroscope = new float[3];
    private long lastFallTime = 0;
    private long lastImpactTime = 0;
    private boolean isMonitoring = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        initSensors();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());
        startMonitoring();
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMonitoring();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Fall Detection Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Monitors for falls and triggers emergency alerts");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        );
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.fall_detection_notification_title))
            .setContentText(getString(R.string.fall_detection_notification_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();
    }
    
    private void initSensors() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        
        if (accelerometer == null) {
            Log.e(TAG, "Accelerometer not available");
        }
        if (gyroscope == null) {
            Log.e(TAG, "Gyroscope not available");
        }
    }
    
    private void startMonitoring() {
        if (accelerometer != null && !isMonitoring) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            if (gyroscope != null) {
                sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_UI);
            }
            isMonitoring = true;
            Log.d(TAG, "Fall detection monitoring started");
        }
    }
    
    private void stopMonitoring() {
        if (isMonitoring) {
            sensorManager.unregisterListener(this);
            isMonitoring = false;
            Log.d(TAG, "Fall detection monitoring stopped");
        }
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            processAccelerometerData(event.values);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            processGyroscopeData(event.values);
        }
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
    
    private void processAccelerometerData(float[] values) {
        float x = values[0];
        float y = values[1];
        float z = values[2];
        
        // Calculate magnitude of acceleration
        float magnitude = (float) Math.sqrt(x * x + y * y + z * z);
        
        // Check for sudden impact (high acceleration)
        if (magnitude > IMPACT_THRESHOLD) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastImpactTime > IMPACT_DETECTION_WINDOW) {
                lastImpactTime = currentTime;
                Log.d(TAG, "Impact detected: " + magnitude);
                checkForFall();
            }
        } else {
            // Debug: Log high acceleration values that don't meet threshold
            if (magnitude > 8.0f) {
                Log.d(TAG, "High acceleration (not impact): " + magnitude + " (threshold: " + IMPACT_THRESHOLD + ")");
            }
        }
        
        // Check for free fall (low acceleration)
        if (magnitude < FALL_THRESHOLD) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastFallTime > FALL_DETECTION_WINDOW) {
                lastFallTime = currentTime;
                Log.d(TAG, "Free fall detected: " + magnitude);
                checkForFall();
            }
        }
        
        // Store current values for next comparison
        lastAcceleration[0] = x;
        lastAcceleration[1] = y;
        lastAcceleration[2] = z;
    }
    
    private void processGyroscopeData(float[] values) {
        // Store gyroscope data for additional fall detection logic
        lastGyroscope[0] = values[0];
        lastGyroscope[1] = values[1];
        lastGyroscope[2] = values[2];
    }
    
    private void checkForFall() {
        // Additional logic to confirm fall based on multiple sensor readings
        // This is a simplified version - in production, you'd want more sophisticated algorithms
        
        long currentTime = System.currentTimeMillis();
        
        Log.d(TAG, "Checking for fall - Impact time: " + lastImpactTime + ", Fall time: " + lastFallTime);
        Log.d(TAG, "Time differences - Impact: " + Math.abs(currentTime - lastImpactTime) + "ms, Fall: " + Math.abs(currentTime - lastFallTime) + "ms");
        
        // Check if we have both impact and free fall within a reasonable time window
        if (Math.abs(currentTime - lastImpactTime) < 2000 && 
            Math.abs(currentTime - lastFallTime) < 2000) {
            
            Log.d(TAG, "Fall detected! Triggering emergency alert");
            triggerEmergencyAlert();
        } else {
            Log.d(TAG, "Fall conditions not met - need both impact and free fall within 2 seconds");
        }
    }
    
    private void triggerEmergencyAlert() {
        Log.d(TAG, "🚨 FALL DETECTED! Triggering emergency alert...");
        
        // Vibrate to get user's attention
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect effect = VibrationEffect.createWaveform(
                    new long[]{0, 1000, 500, 1000, 500, 1000}, 0);
                vibrator.vibrate(effect);
            } else {
                vibrator.vibrate(new long[]{0, 1000, 500, 1000, 500, 1000}, 0);
            }
        }
        
        // Wake up device and show emergency screen even if locked
        wakeUpDeviceAndShowEmergency();
        
        // Stop monitoring temporarily to prevent multiple alerts
        stopMonitoring();
        
        // Restart monitoring after a delay
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isMonitoring) {
                    startMonitoring();
                }
            }
        }, 30000); // 30 seconds delay
    }
    
    private void wakeUpDeviceAndShowEmergency() {
        try {
            // Get PowerManager to wake up the device
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            
            if (powerManager != null) {
                // Create a wake lock to keep screen on
                wakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK | 
                    PowerManager.ACQUIRE_CAUSES_WAKEUP | 
                    PowerManager.ON_AFTER_RELEASE, 
                    "SStep:EmergencyAlert"
                );
                wakeLock.acquire(10000); // Hold for 10 seconds
                Log.d(TAG, "✅ Device woken up with wake lock");
            }
            
            // Create intent for emergency alert
            Intent emergencyIntent = new Intent(this, EmergencyAlertActivity.class);
            emergencyIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK | 
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP |
                Intent.FLAG_ACTIVITY_NO_HISTORY
            );
            
            Log.d(TAG, "🚨 Starting EmergencyAlertActivity");
            startActivity(emergencyIntent);
            
            // Release wake lock after a delay
            if (wakeLock != null) {
                final PowerManager.WakeLock finalWakeLock = wakeLock;
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (finalWakeLock.isHeld()) {
                            finalWakeLock.release();
                            Log.d(TAG, "✅ Wake lock released");
                        }
                    }
                }, 5000);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error showing emergency alert: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback: try basic intent
            try {
                Intent fallbackIntent = new Intent(this, EmergencyAlertActivity.class);
                fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(fallbackIntent);
                Log.d(TAG, "✅ Fallback emergency alert started");
            } catch (Exception fallbackError) {
                Log.e(TAG, "❌ Fallback emergency alert also failed: " + fallbackError.getMessage());
            }
        }
    }
}
