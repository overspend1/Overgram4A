package one.overgram.messenger.ui.liquidglass

import android.content.Context
import android.graphics.*
import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi
import one.overgram.messenger.config.OvergramConfig

/**
 * Helper utilities for liquid glass effects
 */
object LiquidGlassHelper {

    /**
     * Apply window-level blur (Android 12+)
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun applyWindowBlur(window: Window, blurRadius: Int = 20) {
        if (!OvergramConfig.liquidGlassEnabled || !OvergramConfig.liquidGlassUseNativeEffects) {
            return
        }

        try {
            window.setBackgroundBlurRadius(blurRadius)
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Remove window-level blur
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun removeWindowBlur(window: Window) {
        try {
            window.setBackgroundBlurRadius(0)
            window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Check if device supports hardware-accelerated blur
     */
    fun supportsHardwareBlur(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    /**
     * Get recommended blur radius based on device capabilities
     */
    fun getRecommendedBlurRadius(context: Context): Float {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> 25f // Native blur
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 -> 20f // RenderScript
            else -> 10f // Software blur
        }
    }

    /**
     * Create gradient background for glass effects
     */
    fun createGradientBackground(width: Int, height: Int, colors: IntArray): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val gradient = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            colors,
            null,
            Shader.TileMode.CLAMP
        )
        val paint = Paint().apply {
            shader = gradient
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        return bitmap
    }

    /**
     * Apply tint color with alpha blending
     */
    fun applyTint(canvas: Canvas, bounds: RectF, tintColor: Int, cornerRadius: Float = 12f) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tintColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, paint)
    }

    /**
     * Draw glass border
     */
    fun drawGlassBorder(canvas: Canvas, bounds: RectF, opacity: Float = 0.3f, cornerRadius: Float = 12f) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.argb((opacity * 255).toInt(), 255, 255, 255)
        }
        canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, paint)
    }

    /**
     * Estimate performance impact
     */
    fun estimatePerformanceImpact(params: GlassParameters): PerformanceImpact {
        var score = 0

        // Blur radius impact
        score += when {
            params.blurRadius > 30 -> 3
            params.blurRadius > 20 -> 2
            params.blurRadius > 10 -> 1
            else -> 0
        }

        // Noise impact
        if (params.useNoise && params.noiseStrength > 0.02f) {
            score += 1
        }

        // Color adjustments impact
        if (params.saturation != 1.0f || params.brightness != 1.0f) {
            score += 1
        }

        return when {
            score >= 5 -> PerformanceImpact.HIGH
            score >= 3 -> PerformanceImpact.MEDIUM
            else -> PerformanceImpact.LOW
        }
    }

    /**
     * Get battery-optimized parameters
     */
    fun getBatteryOptimizedParameters(baseParams: GlassParameters): GlassParameters {
        return baseParams.copy(
            blurRadius = (baseParams.blurRadius * 0.7f).coerceAtMost(15f),
            useNoise = false,
            saturation = 1.0f,
            brightness = 1.0f,
            adaptiveQuality = true,
            maxBlurRadius = 20
        )
    }

    /**
     * Interpolate between two parameter sets
     */
    fun interpolateParameters(
        from: GlassParameters,
        to: GlassParameters,
        progress: Float
    ): GlassParameters {
        val t = progress.coerceIn(0f, 1f)
        return GlassParameters(
            blurRadius = lerp(from.blurRadius, to.blurRadius, t),
            backgroundOpacity = lerp(from.backgroundOpacity, to.backgroundOpacity, t),
            saturation = lerp(from.saturation, to.saturation, t),
            brightness = lerp(from.brightness, to.brightness, t),
            tintColor = lerpColor(from.tintColor, to.tintColor, t),
            borderOpacity = lerp(from.borderOpacity, to.borderOpacity, t),
            useNoise = if (t < 0.5f) from.useNoise else to.useNoise,
            noiseStrength = lerp(from.noiseStrength, to.noiseStrength, t),
            useHardwareAcceleration = to.useHardwareAcceleration,
            maxBlurRadius = to.maxBlurRadius,
            adaptiveQuality = to.adaptiveQuality
        )
    }

    /**
     * Linear interpolation
     */
    private fun lerp(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t
    }

    /**
     * Color interpolation
     */
    private fun lerpColor(from: Int, to: Int, t: Float): Int {
        val fromA = Color.alpha(from)
        val fromR = Color.red(from)
        val fromG = Color.green(from)
        val fromB = Color.blue(from)

        val toA = Color.alpha(to)
        val toR = Color.red(to)
        val toG = Color.green(to)
        val toB = Color.blue(to)

        return Color.argb(
            (fromA + (toA - fromA) * t).toInt(),
            (fromR + (toR - fromR) * t).toInt(),
            (fromG + (toG - fromG) * t).toInt(),
            (fromB + (toB - fromB) * t).toInt()
        )
    }

    /**
     * Validate parameters and return corrected version
     */
    fun validateParameters(params: GlassParameters): GlassParameters {
        return params.copy(
            blurRadius = params.blurRadius.coerceIn(0f, params.maxBlurRadius.toFloat()),
            backgroundOpacity = params.backgroundOpacity.coerceIn(0f, 1f),
            saturation = params.saturation.coerceIn(0f, 2f),
            brightness = params.brightness.coerceIn(0f, 2f),
            borderOpacity = params.borderOpacity.coerceIn(0f, 1f),
            noiseStrength = params.noiseStrength.coerceIn(0f, 1f)
        )
    }

    /**
     * Create Material You themed parameters
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun createMaterialYouParameters(context: Context, basePreset: GlassPreset = GlassPreset.STANDARD): GlassParameters {
        val params = GlassParameters.fromPreset(basePreset)

        try {
            // Get Material You primary color
            val primaryColor = context.getColor(android.R.color.system_accent1_500)

            // Apply with transparency
            params.tintColor = Color.argb(
                Color.alpha(params.tintColor),
                Color.red(primaryColor),
                Color.green(primaryColor),
                Color.blue(primaryColor)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return params
    }

    /**
     * Export parameters to string for debugging
     */
    fun parametersToString(params: GlassParameters): String {
        return buildString {
            append("GlassParameters(\n")
            append("  blurRadius: ${params.blurRadius}\n")
            append("  backgroundOpacity: ${params.backgroundOpacity}\n")
            append("  saturation: ${params.saturation}\n")
            append("  brightness: ${params.brightness}\n")
            append("  tintColor: #${Integer.toHexString(params.tintColor)}\n")
            append("  borderOpacity: ${params.borderOpacity}\n")
            append("  useNoise: ${params.useNoise}\n")
            append("  noiseStrength: ${params.noiseStrength}\n")
            append("  useHardwareAcceleration: ${params.useHardwareAcceleration}\n")
            append("  maxBlurRadius: ${params.maxBlurRadius}\n")
            append("  adaptiveQuality: ${params.adaptiveQuality}\n")
            append(")")
        }
    }
}

/**
 * Performance impact levels
 */
enum class PerformanceImpact {
    LOW,     // Minimal impact, suitable for all devices
    MEDIUM,  // Moderate impact, may affect older devices
    HIGH     // Significant impact, only for powerful devices
}

/**
 * Animation helper for smooth transitions
 */
class GlassAnimator(
    private val from: GlassParameters,
    private val to: GlassParameters,
    private val duration: Long = 300
) {
    private var startTime = 0L
    private var isRunning = false

    fun start() {
        startTime = System.currentTimeMillis()
        isRunning = true
    }

    fun stop() {
        isRunning = false
    }

    fun getCurrentParameters(): GlassParameters {
        if (!isRunning) return to

        val elapsed = System.currentTimeMillis() - startTime
        val progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)

        if (progress >= 1f) {
            isRunning = false
            return to
        }

        return LiquidGlassHelper.interpolateParameters(from, to, progress)
    }

    fun isAnimating() = isRunning
}
