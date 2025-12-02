/*
 * This is the source code of Overgram for Android.
 *
 * We do not and cannot prevent the use of our code,
 * but be respectful and credit the original author.
 *
 * Copyright @overspend1, 2024
 */

package com.overspend1.overgram.ui.liquidglass;

/**
 * Parameters for liquid glass effect configuration
 */
public class GlassParameters {
    public float blurRadius;        // Blur radius in dp (0-25)
    public float opacity;           // Background opacity (0.0-1.0)
    public float saturation;        // Color saturation multiplier (0.0-2.0)
    public float brightness;        // Brightness adjustment (-1.0 to 1.0)
    public int tintColor;          // Tint color overlay (ARGB)
    public float noiseIntensity;   // Noise texture intensity (0.0-1.0)
    public float borderOpacity;    // Border highlight opacity (0.0-1.0)

    public GlassParameters() {
        // Default values (Standard preset)
        this.blurRadius = 15f;
        this.opacity = 0.75f;
        this.saturation = 1.2f;
        this.brightness = 0.0f;
        this.tintColor = 0x00000000; // Transparent
        this.noiseIntensity = 0.03f;
        this.borderOpacity = 0.15f;
    }

    public GlassParameters(float blurRadius, float opacity, float saturation,
                          float brightness, int tintColor, float noiseIntensity,
                          float borderOpacity) {
        this.blurRadius = blurRadius;
        this.opacity = opacity;
        this.saturation = saturation;
        this.brightness = brightness;
        this.tintColor = tintColor;
        this.noiseIntensity = noiseIntensity;
        this.borderOpacity = borderOpacity;
    }

    /**
     * Create a copy of these parameters
     */
    public GlassParameters copy() {
        return new GlassParameters(
            blurRadius, opacity, saturation, brightness,
            tintColor, noiseIntensity, borderOpacity
        );
    }

    /**
     * Clamp all values to valid ranges
     */
    public void clamp() {
        blurRadius = Math.max(0f, Math.min(25f, blurRadius));
        opacity = Math.max(0f, Math.min(1f, opacity));
        saturation = Math.max(0f, Math.min(2f, saturation));
        brightness = Math.max(-1f, Math.min(1f, brightness));
        noiseIntensity = Math.max(0f, Math.min(1f, noiseIntensity));
        borderOpacity = Math.max(0f, Math.min(1f, borderOpacity));
    }
}
