package com.falldetector.safestep;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import androidx.core.content.ContextCompat;

/**
 * Utility class for handling SVG illustrations
 * This is a placeholder - in production, you would use a library like
 * AndroidSVG or Glide with SVG support to load your SVG files
 */
public class SvgUtils {
    
    /**
     * Load an SVG illustration into an ImageView
     * For now, this loads placeholder drawables
     * In production, replace with actual SVG loading logic
     */
    public static void loadIllustration(Context context, ImageView imageView, int position) {
        int drawableRes;
        
        switch (position) {
            case 0:
                drawableRes = R.drawable.onboarding_1_illustration_png;
                break;
            case 1:
                drawableRes = R.drawable.onboarding_2_illustration_png;
                break;
            case 2:
                drawableRes = R.drawable.onboarding_3_illustration_png;
                break;
            default:
                drawableRes = R.drawable.ic_launcher_foreground;
                break;
        }
        
        Drawable drawable = ContextCompat.getDrawable(context, drawableRes);
        imageView.setImageDrawable(drawable);
    }
}
