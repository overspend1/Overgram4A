package one.overgram.messenger.ui.liquidglass

import android.graphics.Color
import androidx.annotation.ColorInt

/**
 * Parameters for liquid glass effect
 */
data class GlassParameters(
    var blurRadius: Float = 20f,              // Blur intensity (0-100)
    var backgroundOpacity: Float = 0.7f,      // Background transparency (0-1)
    var saturation: Float = 1.2f,             // Color saturation boost (0-2)
    var brightness: Float = 1.05f,            // Brightness adjustment (0-2)
    @ColorInt var tintColor: Int = Color.argb(25, 255, 255, 255), // Glass tint overlay
    var borderOpacity: Float = 0.3f,          // Border/outline opacity
    var useNoise: Boolean = true,             // Add subtle noise texture
    var noiseStrength: Float = 0.02f,         // Noise intensity (0-1)

    // Performance options
    var useHardwareAcceleration: Boolean = true,
    var maxBlurRadius: Int = 50,              // Limit for performance
    var adaptiveQuality: Boolean = true       // Lower quality during animations
) {
    companion object {
        /**
         * Get preset parameters
         */
        fun fromPreset(preset: GlassPreset): GlassParameters {
            return when (preset) {
                GlassPreset.SUBTLE -> GlassParameters(
                    blurRadius = 10f,
                    backgroundOpacity = 0.85f,
                    saturation = 1.1f,
                    brightness = 1.02f,
                    tintColor = Color.argb(15, 255, 255, 255),
                    borderOpacity = 0.2f,
                    noiseStrength = 0.01f
                )

                GlassPreset.STANDARD -> GlassParameters(
                    blurRadius = 20f,
                    backgroundOpacity = 0.7f,
                    saturation = 1.2f,
                    brightness = 1.05f,
                    tintColor = Color.argb(25, 255, 255, 255),
                    borderOpacity = 0.3f,
                    noiseStrength = 0.02f
                )

                GlassPreset.HEAVY -> GlassParameters(
                    blurRadius = 35f,
                    backgroundOpacity = 0.5f,
                    saturation = 1.3f,
                    brightness = 1.1f,
                    tintColor = Color.argb(40, 255, 255, 255),
                    borderOpacity = 0.4f,
                    noiseStrength = 0.03f
                )

                GlassPreset.FROSTED -> GlassParameters(
                    blurRadius = 40f,
                    backgroundOpacity = 0.45f,
                    saturation = 0.9f,
                    brightness = 1.15f,
                    tintColor = Color.argb(50, 240, 240, 245),
                    borderOpacity = 0.5f,
                    noiseStrength = 0.04f
                )

                GlassPreset.CRYSTAL -> GlassParameters(
                    blurRadius = 15f,
                    backgroundOpacity = 0.75f,
                    saturation = 1.5f,
                    brightness = 1.08f,
                    tintColor = Color.argb(20, 255, 255, 255),
                    borderOpacity = 0.35f,
                    noiseStrength = 0.015f
                )

                GlassPreset.MIDNIGHT -> GlassParameters(
                    blurRadius = 30f,
                    backgroundOpacity = 0.6f,
                    saturation = 1.1f,
                    brightness = 0.9f,
                    tintColor = Color.argb(60, 20, 20, 40),
                    borderOpacity = 0.45f,
                    noiseStrength = 0.025f
                )

                GlassPreset.CUSTOM -> GlassParameters() // Use defaults
            }
        }
    }
}

/**
 * Glass effect presets
 */
enum class GlassPreset {
    SUBTLE,      // Light blur, high transparency
    STANDARD,    // Balanced blur and opacity
    HEAVY,       // Strong blur, lower transparency
    FROSTED,     // Heavy blur, matte finish
    CRYSTAL,     // Light blur, vibrant colors
    MIDNIGHT,    // Dark tint, strong blur
    CUSTOM       // User-defined
}
