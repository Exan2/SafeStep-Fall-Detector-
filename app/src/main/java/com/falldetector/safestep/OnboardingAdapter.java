package com.falldetector.safestep;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {
    
    private static final int[] TITLES = {
        R.string.onboarding1_title,
        R.string.onboarding2_title,
        R.string.onboarding3_title
    };
    
    private static final int[] DESCRIPTIONS = {
        R.string.onboarding1_description,
        R.string.onboarding2_description,
        R.string.onboarding3_description
    };
    
    // Onboarding illustrations
    private static final int[] ILLUSTRATIONS = {
        R.drawable.onboarding_1_illustration_png, // Falling man illustration
        R.drawable.onboarding_2_illustration_png, // Location pin illustration
        R.drawable.onboarding_3_illustration_png  // Smartphone illustration
    };
    
    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding, parent, false);
        return new OnboardingViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.bind(position);
    }
    
    @Override
    public int getItemCount() {
        return TITLES.length;
    }
    
    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIllustration;
        private TextView tvTitle;
        private TextView tvDescription;
        
        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIllustration = itemView.findViewById(R.id.iv_illustration);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
        }
        
        public void bind(int position) {
            // Load illustration using utility (supports both drawable and SVG)
            SvgUtils.loadIllustration(itemView.getContext(), ivIllustration, position);
            tvTitle.setText(TITLES[position]);
            tvDescription.setText(DESCRIPTIONS[position]);
        }
    }
}
