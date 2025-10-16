package com.falldetector.safestep;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.widget.ImageView;

public class OnboardingActivity extends AppCompatActivity {
    
    private ViewPager2 viewPager;
    private ImageView dot1, dot2, dot3;
    private Button btnNext;
    private Button btnSkip;
    private int currentPage = 0;
    
    private static final String PREFS_NAME = "SafeStepPrefs";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        
        initViews();
        setupViewPager();
        setupTabLayout();
        updateButtonText();
    }
    
    private void initViews() {
        viewPager = findViewById(R.id.view_pager);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        btnNext = findViewById(R.id.btn_next);
        btnSkip = findViewById(R.id.btn_skip);
        
        btnNext.setOnClickListener(v -> {
            if (currentPage < 2) {
                viewPager.setCurrentItem(currentPage + 1);
            } else {
                completeOnboarding();
            }
        });
        
        btnSkip.setOnClickListener(v -> completeOnboarding());
    }
    
    private void setupViewPager() {
        OnboardingAdapter adapter = new OnboardingAdapter();
        viewPager.setAdapter(adapter);
        
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPage = position;
                updateButtonText();
                updateDots();
            }
        });
    }
    
    private void setupTabLayout() {
        // Custom pagination dots are handled in updateDots()
    }
    
    private void updateButtonText() {
        if (currentPage == 2) {
            btnNext.setText(R.string.get_started);
        } else {
            btnNext.setText(R.string.next);
        }
    }
    
    private void updateDots() {
        // Reset all dots to inactive
        dot1.setImageResource(R.drawable.pagination_dot_inactive);
        dot2.setImageResource(R.drawable.pagination_dot_inactive);
        dot3.setImageResource(R.drawable.pagination_dot_inactive);
        
        // Set current dot to active
        switch (currentPage) {
            case 0:
                dot1.setImageResource(R.drawable.pagination_dot);
                break;
            case 1:
                dot2.setImageResource(R.drawable.pagination_dot);
                break;
            case 2:
                dot3.setImageResource(R.drawable.pagination_dot);
                break;
        }
    }
    
    private void completeOnboarding() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
        
        // Go to User Info screen first
        startActivity(new Intent(this, UserInfoActivity.class));
        finish();
    }
}
