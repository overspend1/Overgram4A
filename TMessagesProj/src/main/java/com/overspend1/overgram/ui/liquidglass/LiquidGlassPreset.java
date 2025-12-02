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
 * Predefined liquid glass presets
 */
public enum LiquidGlassPreset {
    SUBTLE(0, "Subtle", 8f, 0.90f),
    STANDARD(1, "Standard", 15f, 0.75f),
    HEAVY(2, "Heavy", 20f, 0.60f),
    FROSTED(3, "Frosted", 25f, 0.55f),
    CRYSTAL(4, "Crystal", 12f, 0.80f),
    MIDNIGHT(5, "Midnight", 18f, 0.65f);

    public final int id;
    public final String name;
    public final float blurRadius;
    public final float opacity;

    LiquidGlassPreset(int id, String name, float blurRadius, float opacity) {
        this.id = id;
        this.name = name;
        this.blurRadius = blurRadius;
        this.opacity = opacity;
    }

    /**
     * Create glass parameters from this preset
     */
    public GlassParameters toParameters() {
        GlassParameters params = new GlassParameters();
        params.blurRadius = this.blurRadius;
        params.opacity = this.opacity;

        // Preset-specific adjustments
        switch (this) {
            case SUBTLE:
                params.saturation = 1.0f;
                params.brightness = 0.05f;
                params.noiseIntensity = 0.02f;
                params.borderOpacity = 0.10f;
                break;

            case STANDARD:
                params.saturation = 1.2f;
                params.brightness = 0.0f;
                params.noiseIntensity = 0.03f;
                params.borderOpacity = 0.15f;
                break;

            case HEAVY:
                params.saturation = 1.4f;
                params.brightness = -0.05f;
                params.noiseIntensity = 0.05f;
                params.borderOpacity = 0.20f;
                break;

            case FROSTED:
                params.saturation = 0.8f;
                params.brightness = 0.1f;
                params.noiseIntensity = 0.08f;
                params.borderOpacity = 0.25f;
                break;

            case CRYSTAL:
                params.saturation = 1.6f;
                params.brightness = 0.05f;
                params.noiseIntensity = 0.02f;
                params.borderOpacity = 0.30f;
                break;

            case MIDNIGHT:
                params.saturation = 1.0f;
                params.brightness = -0.15f;
                params.noiseIntensity = 0.04f;
                params.borderOpacity = 0.15f;
                params.tintColor = 0x20000000; // Dark tint
                break;
        }

        return params;
    }

    /**
     * Get preset by ID
     */
    public static LiquidGlassPreset fromId(int id) {
        for (LiquidGlassPreset preset : values()) {
            if (preset.id == id) {
                return preset;
            }
        }
        return STANDARD;
    }

    /**
     * Get description for this preset
     */
    public String getDescription() {
        switch (this) {
            case SUBTLE:
                return "Minimal distraction with light blur";
            case STANDARD:
                return "Balanced glass effect (recommended)";
            case HEAVY:
                return "Strong glassmorphism appearance";
            case FROSTED:
                return "Matte frosted glass finish";
            case CRYSTAL:
                return "Vibrant colors with medium blur";
            case MIDNIGHT:
                return "Optimized for dark themes";
            default:
                return "";
        }
    }
}
