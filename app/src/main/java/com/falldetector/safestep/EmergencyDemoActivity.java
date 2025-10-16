package com.falldetector.safestep;

import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class EmergencyDemoActivity extends AppCompatActivity {
    
    private ImageView ivEmergencyIllustration;
    private ImageView ivPulse;
    private TextView tvTitle;
    private TextView tvDescription;
    private TextView tvDemoCountdown;
    private Button btnIUnderstand;
    private Animation shimmerAnim;
    private Vibrator vibrator;
    private ObjectAnimator pulseAnimator;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_demo);
        
        initViews();
        setupEffects();
        setupClickListeners();
    }
    
    private void initViews() {
        ivEmergencyIllustration = findViewById(R.id.iv_emergency_illustration);
        ivPulse = findViewById(R.id.iv_pulse);
        tvTitle = findViewById(R.id.tv_title);
        tvDescription = findViewById(R.id.tv_description);
        tvDemoCountdown = findViewById(R.id.tv_demo_countdown);
        btnIUnderstand = findViewById(R.id.btn_i_understand);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }
    
    private void setupEffects() {
        // Replace drawable-internal animation with view animation for unconstrained scaling
        if (ivPulse != null) {
            PropertyValuesHolder sx = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.3f, 2.2f);
            PropertyValuesHolder sy = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.3f, 2.2f);
            PropertyValuesHolder a = PropertyValuesHolder.ofFloat(View.ALPHA, 0.95f, 0.05f);
            pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(ivPulse, sx, sy, a);
            pulseAnimator.setDuration(2000);
            pulseAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            pulseAnimator.setRepeatMode(ObjectAnimator.REVERSE);
            pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            pulseAnimator.start();
        }
        // Shimmer-like animation for the demo countdown label
        shimmerAnim = new AlphaAnimation(0.4f, 1.0f);
        shimmerAnim.setDuration(600);
        shimmerAnim.setRepeatMode(Animation.REVERSE);
        shimmerAnim.setRepeatCount(Animation.INFINITE);
        if (tvDemoCountdown != null) tvDemoCountdown.startAnimation(shimmerAnim);
    }
    
    private void setupClickListeners() {
        btnIUnderstand.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(50);
                }
            }
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tvDemoCountdown != null) tvDemoCountdown.clearAnimation();
        if (pulseAnimator != null) pulseAnimator.cancel();
    }
}
