package one.overgram.messenger.ui.liquidglass

import android.content.Context
import android.graphics.*
import android.view.View
import androidx.core.graphics.applyCanvas
import one.overgram.messenger.config.OvergramConfig

/**
 * Chat bubble with liquid glass effect
 *
 * This class can be used to add glass effects to Telegram chat message cells.
 * It captures the background, applies blur and color adjustments, and renders
 * a beautiful glassmorphic bubble.
 */
class GlassChatBubble(private val context: Context) {

    private var glassEffect: LiquidGlassEffect? = null
    private var backgroundCache: Bitmap? = null
    private var lastCaptureTime = 0L
    private val cacheDuration = 100L // ms

    companion object {
        private const val DEFAULT_CORNER_RADIUS = 18f // dp
        private const val CACHE_KEY_PREFIX = "chat_bubble_"
    }

    init {
        initializeGlassEffect()
    }

    /**
     * Initialize or reinitialize glass effect with current settings
     */
    private fun initializeGlassEffect() {
        if (OvergramConfig.liquidGlassEnabled) {
            val params = getCurrentParameters()
            glassEffect = LiquidGlassEffect(context, params)
        }
    }

    /**
     * Get current glass parameters from config
     */
    private fun getCurrentParameters(): GlassParameters {
        val preset = OvergramConfig.liquidGlassPreset
        val params = GlassParameters.fromPreset(preset)

        // Override with custom values if using custom preset
        if (preset == GlassPreset.CUSTOM) {
            params.blurRadius = OvergramConfig.liquidGlassBlurRadius
            params.backgroundOpacity = OvergramConfig.liquidGlassOpacity
            params.saturation = OvergramConfig.liquidGlassSaturation
            params.brightness = OvergramConfig.liquidGlassBrightness
            params.borderOpacity = OvergramConfig.liquidGlassBorderOpacity
            params.useNoise = OvergramConfig.liquidGlassUseNoise
            params.noiseStrength = OvergramConfig.liquidGlassNoiseStrength
        }

        params.adaptiveQuality = OvergramConfig.liquidGlassAdaptiveQuality
        params.maxBlurRadius = OvergramConfig.liquidGlassMaxBlurRadius

        return params
    }

    /**
     * Draw glass bubble on the given canvas
     *
     * @param canvas Canvas to draw on
     * @param bounds Bounds of the bubble
     * @param view The message view (for background capture)
     * @param isOutgoing Whether this is an outgoing message
     * @param cornerRadius Corner radius in dp
     */
    fun draw(
        canvas: Canvas,
        bounds: RectF,
        view: View,
        isOutgoing: Boolean = false,
        cornerRadius: Float = DEFAULT_CORNER_RADIUS
    ) {
        if (!OvergramConfig.liquidGlassEnabled || !OvergramConfig.liquidGlassApplyToChatBubbles) {
            // Draw standard bubble without glass effect
            drawStandardBubble(canvas, bounds, isOutgoing, cornerRadius)
            return
        }

        // Ensure glass effect is initialized
        if (glassEffect == null) {
            initializeGlassEffect()
        }

        val effect = glassEffect ?: run {
            drawStandardBubble(canvas, bounds, isOutgoing, cornerRadius)
            return
        }

        // Capture background
        val background = captureBackground(view, bounds)

        if (background != null && !background.isRecycled) {
            // Apply glass effect
            canvas.save()

            // Clip to rounded rectangle
            val path = Path().apply {
                addRoundRect(bounds, cornerRadius, cornerRadius, Path.Direction.CW)
            }
            canvas.clipPath(path)

            // Draw glass effect
            effect.apply(canvas, bounds, background)

            canvas.restore()

            // Optionally draw message-specific tint for outgoing messages
            if (isOutgoing && OvergramConfig.liquidGlassEnabled) {
                drawOutgoingTint(canvas, bounds, cornerRadius)
            }
        } else {
            // Fallback to standard bubble if background capture failed
            drawStandardBubble(canvas, bounds, isOutgoing, cornerRadius)
        }
    }

    /**
     * Draw glass bubble with caching
     *
     * @param canvas Canvas to draw on
     * @param bounds Bounds of the bubble
     * @param view The message view
     * @param messageId Unique message ID for cache key
     * @param isOutgoing Whether this is an outgoing message
     */
    fun drawWithCache(
        canvas: Canvas,
        bounds: RectF,
        view: View,
        messageId: Long,
        isOutgoing: Boolean = false
    ) {
        if (!OvergramConfig.liquidGlassEnabled || !OvergramConfig.liquidGlassApplyToChatBubbles) {
            drawStandardBubble(canvas, bounds, isOutgoing)
            return
        }

        val effect = glassEffect ?: run {
            initializeGlassEffect()
            glassEffect ?: return drawStandardBubble(canvas, bounds, isOutgoing)
        }

        val background = captureBackground(view, bounds)
        val cacheKey = "$CACHE_KEY_PREFIX$messageId"

        if (background != null && !background.isRecycled) {
            canvas.save()

            val path = Path().apply {
                addRoundRect(bounds, DEFAULT_CORNER_RADIUS, DEFAULT_CORNER_RADIUS, Path.Direction.CW)
            }
            canvas.clipPath(path)

            effect.applyWithCache(canvas, bounds, background, cacheKey)

            canvas.restore()

            if (isOutgoing) {
                drawOutgoingTint(canvas, bounds)
            }
        } else {
            drawStandardBubble(canvas, bounds, isOutgoing)
        }
    }

    /**
     * Capture background behind the view
     */
    private fun captureBackground(view: View, bounds: RectF): Bitmap? {
        try {
            // Check cache
            val now = System.currentTimeMillis()
            if (backgroundCache != null && !backgroundCache!!.isRecycled && (now - lastCaptureTime) < cacheDuration) {
                return backgroundCache
            }

            // Get parent view
            val parent = view.parent as? View ?: return null

            val width = bounds.width().toInt()
            val height = bounds.height().toInt()

            if (width <= 0 || height <= 0) return null

            // Create bitmap
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Translate to capture correct region
            canvas.save()
            canvas.translate(-bounds.left, -bounds.top)
            canvas.translate(-view.left.toFloat(), -view.top.toFloat())

            // Draw parent background
            parent.background?.draw(canvas)

            // Draw parent content (excluding current view)
            parent.draw(canvas)

            canvas.restore()

            // Update cache
            backgroundCache?.recycle()
            backgroundCache = bitmap
            lastCaptureTime = now

            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Draw standard (non-glass) bubble
     */
    private fun drawStandardBubble(
        canvas: Canvas,
        bounds: RectF,
        isOutgoing: Boolean = false,
        cornerRadius: Float = DEFAULT_CORNER_RADIUS
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isOutgoing) {
                Color.rgb(220, 248, 198) // Light green for outgoing
            } else {
                Color.WHITE // White for incoming
            }
            style = Paint.Style.FILL
        }

        canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, paint)

        // Draw subtle border
        paint.apply {
            color = Color.argb(30, 0, 0, 0)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, paint)
    }

    /**
     * Draw subtle tint for outgoing messages
     */
    private fun drawOutgoingTint(
        canvas: Canvas,
        bounds: RectF,
        cornerRadius: Float = DEFAULT_CORNER_RADIUS
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            // Light green tint
            color = Color.argb(15, 100, 200, 100)
            style = Paint.Style.FILL
        }

        canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, paint)
    }

    /**
     * Set whether view is currently animating
     */
    fun setAnimating(animating: Boolean) {
        glassEffect?.setAnimating(animating)
    }

    /**
     * Update glass parameters from config
     */
    fun updateParameters() {
        if (OvergramConfig.liquidGlassEnabled) {
            val params = getCurrentParameters()
            glassEffect?.setParameters(params)
        }
    }

    /**
     * Clear background cache
     */
    fun clearCache() {
        backgroundCache?.recycle()
        backgroundCache = null
        glassEffect?.clearCache()
    }

    /**
     * Release resources
     */
    fun destroy() {
        clearCache()
        glassEffect?.destroy()
        glassEffect = null
    }
}

/**
 * Extension function for easy integration into message cells
 */
fun View.drawGlassBubble(
    canvas: Canvas,
    bounds: RectF,
    messageId: Long,
    isOutgoing: Boolean = false,
    glassBubble: GlassChatBubble? = null
): GlassChatBubble {
    val bubble = glassBubble ?: GlassChatBubble(context)
    bubble.drawWithCache(canvas, bounds, this, messageId, isOutgoing)
    return bubble
}
