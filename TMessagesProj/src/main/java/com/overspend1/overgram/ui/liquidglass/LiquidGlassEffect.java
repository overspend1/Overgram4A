/*
 * This is the source code of Overgram for Android.
 *
 * We do not and cannot prevent the use of our code,
 * but be respectful and credit the original author.
 *
 * Copyright @overspend1, 2024
 */

package com.overspend1.overgram.ui.liquidglass;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Utilities;

/**
 * Core liquid glass effect implementation
 * Applies blur, tint, and glassmorphism effects to bitmaps
 */
public class LiquidGlassEffect {
    private GlassParameters parameters;
    private Paint blurPaint;
    private Paint tintPaint;
    private Paint noisePaint;
    private Paint borderPaint;

    private Bitmap cachedBlurredBitmap;
    private long lastCacheTime;
    private static final long CACHE_DURATION = 100; // ms

    public LiquidGlassEffect(GlassParameters parameters) {
        this.parameters = parameters;
        initPaints();
    }

    private void initPaints() {
        // Blur paint with color adjustments
        blurPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        blurPaint.setAlpha((int) (parameters.opacity * 255));

        // Apply saturation and brightness
        ColorMatrix colorMatrix = new ColorMatrix();
        adjustColorMatrix(colorMatrix, parameters.saturation, parameters.brightness);
        blurPaint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));

        // Tint paint
        tintPaint = new Paint();
        tintPaint.setColor(parameters.tintColor);
        tintPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

        // Noise paint
        noisePaint = new Paint();
        noisePaint.setAlpha((int) (parameters.noiseIntensity * 255));

        // Border paint
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(AndroidUtilities.dp(1));
        borderPaint.setColor(0xFFFFFFFF);
        borderPaint.setAlpha((int) (parameters.borderOpacity * 255));
    }

    /**
     * Apply glass effect to a canvas region
     * @param canvas Target canvas
     * @param bounds Region to apply effect
     * @param backgroundBitmap Background image behind the region
     */
    public void apply(Canvas canvas, RectF bounds, Bitmap backgroundBitmap) {
        if (backgroundBitmap == null || backgroundBitmap.isRecycled()) {
            return;
        }

        try {
            // Extract and blur the background region
            Bitmap blurred = getBlurredBitmap(backgroundBitmap, bounds);
            if (blurred != null && !blurred.isRecycled()) {
                // Draw blurred background
                canvas.drawBitmap(blurred, null, bounds, blurPaint);

                // Apply tint overlay
                if (parameters.tintColor != 0) {
                    canvas.drawRect(bounds, tintPaint);
                }

                // Add subtle noise texture (optional)
                if (parameters.noiseIntensity > 0.01f) {
                    drawNoiseTexture(canvas, bounds);
                }

                // Draw border highlight
                if (parameters.borderOpacity > 0.05f) {
                    canvas.drawRoundRect(bounds, AndroidUtilities.dp(12), AndroidUtilities.dp(12), borderPaint);
                }
            }
        } catch (Exception e) {
            // Fallback to simple semi-transparent background
            Paint fallbackPaint = new Paint();
            fallbackPaint.setColor(0x80000000);
            canvas.drawRoundRect(bounds, AndroidUtilities.dp(12), AndroidUtilities.dp(12), fallbackPaint);
        }
    }

    /**
     * Get blurred version of bitmap region (with caching)
     */
    private Bitmap getBlurredBitmap(Bitmap source, RectF bounds) {
        long currentTime = System.currentTimeMillis();

        // Return cached blur if still valid
        if (cachedBlurredBitmap != null &&
            !cachedBlurredBitmap.isRecycled() &&
            (currentTime - lastCacheTime) < CACHE_DURATION) {
            return cachedBlurredBitmap;
        }

        try {
            // Extract region
            int left = Math.max(0, (int) bounds.left);
            int top = Math.max(0, (int) bounds.top);
            int width = Math.min(source.getWidth() - left, (int) bounds.width());
            int height = Math.min(source.getHeight() - top, (int) bounds.height());

            if (width <= 0 || height <= 0) {
                return null;
            }

            Bitmap region = Bitmap.createBitmap(source, left, top, width, height);

            // Apply blur
            Bitmap blurred = applyBlur(region, parameters.blurRadius);

            // Cache result
            if (cachedBlurredBitmap != null && !cachedBlurredBitmap.isRecycled()) {
                cachedBlurredBitmap.recycle();
            }
            cachedBlurredBitmap = blurred;
            lastCacheTime = currentTime;

            // Clean up temporary bitmap
            if (region != blurred) {
                region.recycle();
            }

            return blurred;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Apply blur to bitmap using Utilities.blurBitmap (from Telegram)
     */
    private Bitmap applyBlur(Bitmap bitmap, float radius) {
        if (radius < 1f) {
            return bitmap;
        }

        try {
            // Use Telegram's optimized blur implementation
            int blurRadius = (int) Math.min(25, radius);
            return Utilities.blurBitmap(bitmap, blurRadius, 1, bitmap.getWidth(), bitmap.getHeight(), bitmap.getRowBytes());
        } catch (Exception e) {
            return bitmap;
        }
    }

    /**
     * Adjust color matrix for saturation and brightness
     */
    private void adjustColorMatrix(ColorMatrix matrix, float saturation, float brightness) {
        // Saturation
        float sat = saturation;
        float invSat = 1.0f - sat;
        float R = 0.213f * invSat;
        float G = 0.715f * invSat;
        float B = 0.072f * invSat;

        float[] satMatrix = new float[] {
            R + sat, G,       B,       0, 0,
            R,       G + sat, B,       0, 0,
            R,       G,       B + sat, 0, 0,
            0,       0,       0,       1, 0
        };
        matrix.set(satMatrix);

        // Brightness
        float b = brightness * 255;
        ColorMatrix brightnessMatrix = new ColorMatrix(new float[] {
            1, 0, 0, 0, b,
            0, 1, 0, 0, b,
            0, 0, 1, 0, b,
            0, 0, 0, 1, 0
        });
        matrix.postConcat(brightnessMatrix);
    }

    /**
     * Draw subtle noise texture for realistic glass appearance
     */
    private void drawNoiseTexture(Canvas canvas, RectF bounds) {
        // Simple noise using random pixels (can be optimized with a noise bitmap)
        Paint noisePaint = new Paint();
        noisePaint.setAlpha((int) (parameters.noiseIntensity * 50));

        for (int i = 0; i < 100; i++) {
            float x = bounds.left + (float) Math.random() * bounds.width();
            float y = bounds.top + (float) Math.random() * bounds.height();
            noisePaint.setColor(Math.random() > 0.5 ? 0xFFFFFFFF : 0xFF000000);
            canvas.drawCircle(x, y, 0.5f, noisePaint);
        }
    }

    /**
     * Update parameters and refresh paints
     */
    public void setParameters(GlassParameters parameters) {
        this.parameters = parameters;
        initPaints();
        clearCache();
    }

    /**
     * Clear cached blurred bitmap
     */
    public void clearCache() {
        if (cachedBlurredBitmap != null && !cachedBlurredBitmap.isRecycled()) {
            cachedBlurredBitmap.recycle();
            cachedBlurredBitmap = null;
        }
    }

    /**
     * Release resources
     */
    public void recycle() {
        clearCache();
    }
}
