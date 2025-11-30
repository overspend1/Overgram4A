package one.overgram.messenger.ui.liquidglass

import android.content.Context
import android.graphics.*
import android.os.Build
import android.renderscript.*
import androidx.annotation.RequiresApi
import kotlin.math.min
import kotlin.random.Random

/**
 * Main liquid glass effect implementation
 */
class LiquidGlassEffect(
    private val context: Context,
    private var params: GlassParameters = GlassParameters()
) {
    private val cache = mutableMapOf<String, CachedBlur>()
    private var isAnimating = false
    private var lastRenderTime = 0L
    private var framesSinceLastClear = 0

    // RenderScript (API 17-30)
    private var renderScript: RenderScript? = null
    private var blurScript: ScriptIntrinsicBlur? = null

    companion object {
        private const val CACHE_LIFETIME_MS = 5000L
        private const val MAX_CACHE_ENTRIES = 50

        /**
         * Check if device supports DWM/native blur
         */
        @JvmStatic
        fun supportsNativeBlur(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S // Android 12+
        }
    }

    init {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                renderScript = RenderScript.create(context)
                blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Apply glass effect to canvas
     */
    fun apply(canvas: Canvas, bounds: RectF, background: Bitmap?) {
        if (background == null || background.isRecycled) {
            // Fallback to transparent
            val paint = Paint().apply {
                color = Color.argb((params.backgroundOpacity * 255).toInt(), 0, 0, 0)
            }
            canvas.drawRoundRect(bounds, 12f, 12f, paint)
            return
        }

        // Apply blur
        val effectiveRadius = if (isAnimating && params.adaptiveQuality) {
            params.blurRadius * 0.5f
        } else {
            params.blurRadius
        }

        val blurred = applyBlur(background, effectiveRadius)

        // Apply color adjustments
        val adjusted = applyColorAdjustments(blurred)

        // Apply noise
        val final = if (params.useNoise) {
            applyNoise(adjusted)
        } else {
            adjusted
        }

        // Draw blurred background
        val paint = Paint().apply {
            alpha = (params.backgroundOpacity * 255).toInt()
        }
        canvas.save()
        canvas.clipRect(bounds)
        canvas.drawBitmap(final, bounds.left, bounds.top, paint)
        canvas.restore()

        // Apply tint overlay
        if (Color.alpha(params.tintColor) > 0) {
            val tintPaint = Paint().apply {
                color = params.tintColor
            }
            canvas.drawRoundRect(bounds, 12f, 12f, tintPaint)
        }

        // Draw border
        if (params.borderOpacity > 0) {
            applyBorder(canvas, bounds)
        }

        // Cleanup
        if (final != background && !final.isRecycled) {
            final.recycle()
        }
    }

    /**
     * Apply blur with caching
     */
    fun applyWithCache(canvas: Canvas, bounds: RectF, background: Bitmap?, cacheKey: String) {
        val now = System.currentTimeMillis()

        // Check cache
        cache[cacheKey]?.let { cached ->
            if (cached.region == bounds && (now - cached.timestamp) < CACHE_LIFETIME_MS) {
                // Use cached
                val paint = Paint().apply {
                    alpha = (params.backgroundOpacity * 255).toInt()
                }
                canvas.drawBitmap(cached.blurred, bounds.left, bounds.top, paint)

                if (Color.alpha(params.tintColor) > 0) {
                    canvas.drawRoundRect(bounds, 12f, 12f, Paint().apply { color = params.tintColor })
                }
                if (params.borderOpacity > 0) {
                    applyBorder(canvas, bounds)
                }
                return
            }
        }

        // Not cached, render and cache
        if (background != null && !background.isRecycled) {
            val blurred = applyBlur(background, params.blurRadius)
            val adjusted = applyColorAdjustments(blurred)
            val final = if (params.useNoise) applyNoise(adjusted) else adjusted

            // Store in cache
            cache[cacheKey] = CachedBlur(final.copy(final.config, true), bounds, now)

            // Limit cache size
            if (cache.size > MAX_CACHE_ENTRIES) {
                val oldestKey = cache.entries.minByOrNull { it.value.timestamp }?.key
                oldestKey?.let { key ->
                    cache[key]?.blurred?.recycle()
                    cache.remove(key)
                }
            }

            // Draw
            apply(canvas, bounds, background)
        }
    }

    /**
     * Apply blur effect
     */
    private fun applyBlur(source: Bitmap, radius: Float): Bitmap {
        if (radius <= 0f) return source

        val effectiveRadius = min(radius, params.maxBlurRadius.toFloat())

        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                // Use RenderEffect API (Android 12+)
                applyRenderEffectBlur(source, effectiveRadius)
            }
            renderScript != null && blurScript != null -> {
                // Use RenderScript (Android 8-11)
                applyRenderScriptBlur(source, effectiveRadius)
            }
            else -> {
                // Fallback to software blur
                applyFastBlur(source, effectiveRadius.toInt())
            }
        }
    }

    /**
     * RenderEffect blur (Android 12+)
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun applyRenderEffectBlur(source: Bitmap, radius: Float): Bitmap {
        // Note: RenderEffect is typically used on Views, not Bitmaps directly
        // For bitmap blur on Android 12+, still use RenderScript or software
        return if (renderScript != null) {
            applyRenderScriptBlur(source, radius)
        } else {
            applyFastBlur(source, radius.toInt())
        }
    }

    /**
     * RenderScript blur
     */
    private fun applyRenderScriptBlur(source: Bitmap, radius: Float): Bitmap {
        try {
            val rs = renderScript ?: return source
            val script = blurScript ?: return source

            val input = Allocation.createFromBitmap(rs, source)
            val output = Allocation.createTyped(rs, input.type)

            script.setRadius(min(radius, 25f)) // RenderScript max is 25
            script.setInput(input)
            script.forEach(output)

            val result = Bitmap.createBitmap(source.width, source.height, source.config)
            output.copyTo(result)

            input.destroy()
            output.destroy()

            return result
        } catch (e: Exception) {
            e.printStackTrace()
            return source
        }
    }

    /**
     * Fast blur algorithm (fallback)
     */
    private fun applyFastBlur(source: Bitmap, radius: Int): Bitmap {
        if (radius < 1) return source

        val w = source.width
        val h = source.height
        val pix = IntArray(w * h)
        source.getPixels(pix, 0, w, 0, 0, w, h)

        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1

        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)

        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int

        val vmin = IntArray(maxOf(w, h))

        var divsum = (div + 1) shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        i = 0
        while (i < 256 * divsum) {
            dv[i] = i / divsum
            i++
        }

        yw = 0
        yi = 0

        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int

        y = 0
        while (y < h) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum

            i = -radius
            while (i <= radius) {
                p = pix[yi + minOf(wm, maxOf(i, 0))]
                sir = stack[i + radius]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 - kotlin.math.abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                i++
            }
            stackpointer = radius

            x = 0
            while (x < w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (y == 0) {
                    vmin[x] = minOf(x + radius + 1, wm)
                }
                p = pix[yw + vmin[x]]

                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi++
                x++
            }
            yw += w
            y++
        }

        x = 0
        while (x < w) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = maxOf(0, yp) + x

                sir = stack[i + radius]

                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]

                rbs = r1 - kotlin.math.abs(i)

                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs

                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }

                if (i < hm) {
                    yp += w
                }
                i++
            }
            yi = x
            stackpointer = radius
            y = 0
            while (y < h) {
                pix[yi] = -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (x == 0) {
                    vmin[y] = minOf(y + r1, hm) * w
                }
                p = x + vmin[y]

                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi += w
                y++
            }
            x++
        }

        val result = Bitmap.createBitmap(w, h, source.config)
        result.setPixels(pix, 0, w, 0, 0, w, h)
        return result
    }

    /**
     * Apply color adjustments
     */
    private fun applyColorAdjustments(bitmap: Bitmap): Bitmap {
        if (params.saturation == 1.0f && params.brightness == 1.0f) {
            return bitmap
        }

        val result = bitmap.copy(bitmap.config, true)
        val canvas = Canvas(result)
        val paint = Paint()

        val cm = ColorMatrix()
        cm.setSaturation(params.saturation)

        if (params.brightness != 1.0f) {
            val scale = params.brightness
            val translate = (1f - scale) * 255
            val matrix = ColorMatrix(floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            ))
            cm.postConcat(matrix)
        }

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return result
    }

    /**
     * Apply noise texture
     */
    private fun applyNoise(bitmap: Bitmap): Bitmap {
        if (params.noiseStrength <= 0f) return bitmap

        val result = bitmap.copy(bitmap.config, true)
        val strength = (params.noiseStrength * 255).toInt()

        for (y in 0 until result.height) {
            for (x in 0 until result.width) {
                val pixel = result.getPixel(x, y)
                val noise = Random.nextInt(-strength, strength + 1)

                val r = ((pixel shr 16 and 0xFF) + noise).coerceIn(0, 255)
                val g = ((pixel shr 8 and 0xFF) + noise).coerceIn(0, 255)
                val b = ((pixel and 0xFF) + noise).coerceIn(0, 255)
                val a = pixel shr 24 and 0xFF

                result.setPixel(x, y, Color.argb(a, r, g, b))
            }
        }

        return result
    }

    /**
     * Draw border
     */
    private fun applyBorder(canvas: Canvas, bounds: RectF) {
        val paint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.argb((params.borderOpacity * 255).toInt(), 255, 255, 255)
            isAntiAlias = true
        }
        canvas.drawRoundRect(bounds, 12f, 12f, paint)
    }

    /**
     * Update parameters
     */
    fun setParameters(newParams: GlassParameters) {
        params = newParams
        clearCache()
    }

    fun getParameters() = params

    /**
     * Set animating state
     */
    fun setAnimating(animating: Boolean) {
        isAnimating = animating
    }

    /**
     * Clear cache
     */
    fun clearCache() {
        cache.values.forEach { it.blurred.recycle() }
        cache.clear()
    }

    fun clearCache(key: String) {
        cache[key]?.blurred?.recycle()
        cache.remove(key)
    }

    /**
     * Cleanup
     */
    fun destroy() {
        clearCache()
        blurScript?.destroy()
        renderScript?.destroy()
    }

    /**
     * Cached blur data
     */
    private data class CachedBlur(
        val blurred: Bitmap,
        val region: RectF,
        val timestamp: Long
    )
}
