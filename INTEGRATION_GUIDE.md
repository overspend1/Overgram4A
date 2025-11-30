# Liquid Glass Integration Guide

Complete guide for integrating liquid glass effects into Overgram for Android.

## Quick Start

### 1. Enable Liquid Glass in Your Activity

```kotlin
import one.overgram.messenger.config.OvergramConfig
import one.overgram.messenger.ui.liquidglass.LiquidGlassHelper

class ChatActivity : BaseFragment() {
    override fun onFragmentCreate(): Boolean {
        super.onFragmentCreate()

        // Apply window blur on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            parentActivity?.window?.let { window ->
                LiquidGlassHelper.applyWindowBlur(window, 20)
            }
        }

        return true
    }
}
```

### 2. Add Glass to Message Bubbles

#### Option A: Using GlassChatBubble (Recommended)

```kotlin
import one.overgram.messenger.ui.liquidglass.GlassChatBubble

class ChatMessageCell : BaseCell {
    private var glassBubble: GlassChatBubble? = null

    init {
        glassBubble = GlassChatBubble(context)
    }

    override fun onDraw(canvas: Canvas) {
        val bounds = RectF(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            (width - paddingRight).toFloat(),
            (height - paddingBottom).toFloat()
        )

        // Draw glass bubble
        glassBubble?.drawWithCache(
            canvas = canvas,
            bounds = bounds,
            view = this,
            messageId = currentMessageObject.id.toLong(),
            isOutgoing = currentMessageObject.isOutOwner
        )

        // Draw message content on top
        super.onDraw(canvas)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        glassBubble?.destroy()
    }
}
```

#### Option B: Using Extension Function

```kotlin
import one.overgram.messenger.ui.liquidglass.drawGlassBubble

class ChatMessageCell : BaseCell {
    private var glassBubble: GlassChatBubble? = null

    override fun onDraw(canvas: Canvas) {
        val bounds = RectF(0f, 0f, width.toFloat(), height.toFloat())

        // Simple one-liner
        glassBubble = drawGlassBubble(
            canvas = canvas,
            bounds = bounds,
            messageId = currentMessageObject.id.toLong(),
            isOutgoing = currentMessageObject.isOutOwner,
            glassBubble = glassBubble
        )

        super.onDraw(canvas)
    }
}
```

### 3. Add Settings Entry Point

```kotlin
// In SettingsActivity.kt or similar
import one.overgram.messenger.ui.settings.LiquidGlassSettingsActivity

private fun openLiquidGlassSettings() {
    presentFragment(LiquidGlassSettingsActivity())
}
```

## Advanced Integration

### Custom Glass Effects for Specific Views

```kotlin
import one.overgram.messenger.ui.liquidglass.LiquidGlassEffect
import one.overgram.messenger.ui.liquidglass.GlassParameters
import one.overgram.messenger.ui.liquidglass.GlassPreset

class CustomGlassView(context: Context) : View(context) {
    private val glassEffect: LiquidGlassEffect
    private val params = GlassParameters.fromPreset(GlassPreset.CRYSTAL)

    init {
        // Customize parameters
        params.blurRadius = 25f
        params.tintColor = Color.argb(40, 100, 150, 255)

        glassEffect = LiquidGlassEffect(context, params)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val background = captureBackground()
        val bounds = RectF(0f, 0f, width.toFloat(), height.toFloat())

        glassEffect.apply(canvas, bounds, background)
    }

    private fun captureBackground(): Bitmap {
        // Implement background capture logic
        // See GlassChatBubble.kt for reference
        // ...
    }
}
```

### Animated Glass Transitions

```kotlin
import one.overgram.messenger.ui.liquidglass.GlassAnimator

class AnimatedGlassView(context: Context) : View(context) {
    private val glassEffect = LiquidGlassEffect(context)
    private var animator: GlassAnimator? = null

    fun transitionToPreset(preset: GlassPreset) {
        val currentParams = glassEffect.getParameters()
        val newParams = GlassParameters.fromPreset(preset)

        animator = GlassAnimator(currentParams, newParams, duration = 500)
        animator?.start()

        // Invalidate on animation frames
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                animator?.let { anim ->
                    if (anim.isAnimating()) {
                        val params = anim.getCurrentParameters()
                        glassEffect.setParameters(params)
                        invalidate()
                        handler.postDelayed(this, 16) // ~60 FPS
                    }
                }
            }
        }
        handler.post(runnable)
    }
}
```

### Material You Integration

```kotlin
import one.overgram.messenger.ui.liquidglass.LiquidGlassHelper

@RequiresApi(Build.VERSION_CODES.S)
fun setupMaterialYouGlass(context: Context) {
    val params = LiquidGlassHelper.createMaterialYouParameters(
        context = context,
        basePreset = GlassPreset.STANDARD
    )

    // Apply to your glass effect
    glassEffect.setParameters(params)
}
```

### Performance Optimization

```kotlin
import one.overgram.messenger.ui.liquidglass.LiquidGlassHelper
import one.overgram.messenger.ui.liquidglass.PerformanceImpact

class OptimizedGlassView(context: Context) : View(context) {
    private val glassEffect = LiquidGlassEffect(context)

    init {
        setupOptimalParameters()
    }

    private fun setupOptimalParameters() {
        val params = GlassParameters.fromPreset(GlassPreset.STANDARD)

        // Check performance impact
        val impact = LiquidGlassHelper.estimatePerformanceImpact(params)

        val finalParams = when (impact) {
            PerformanceImpact.HIGH -> {
                // Use battery-optimized parameters
                LiquidGlassHelper.getBatteryOptimizedParameters(params)
            }
            PerformanceImpact.MEDIUM -> {
                // Slightly reduce quality
                params.copy(
                    blurRadius = params.blurRadius * 0.85f,
                    adaptiveQuality = true
                )
            }
            PerformanceImpact.LOW -> params
        }

        // Validate before applying
        glassEffect.setParameters(LiquidGlassHelper.validateParameters(finalParams))
    }

    override fun onScrollStateChanged(state: Int) {
        // Reduce quality during scroll
        when (state) {
            SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING -> {
                glassEffect.setAnimating(true)
            }
            SCROLL_STATE_IDLE -> {
                glassEffect.setAnimating(false)
            }
        }
    }
}
```

## Configuration

### Reading Settings

```kotlin
import one.overgram.messenger.config.OvergramConfig

// Check if glass is enabled
if (OvergramConfig.liquidGlassEnabled) {
    // Apply glass effects
}

// Get current preset
val preset = OvergramConfig.liquidGlassPreset

// Get individual parameters
val blurRadius = OvergramConfig.liquidGlassBlurRadius
val opacity = OvergramConfig.liquidGlassOpacity
```

### Writing Settings

```kotlin
// Enable/disable
OvergramConfig.liquidGlassEnabled = true

// Change preset
OvergramConfig.liquidGlassPreset = GlassPreset.CRYSTAL

// Custom parameters
OvergramConfig.liquidGlassBlurRadius = 25f
OvergramConfig.liquidGlassOpacity = 0.8f
OvergramConfig.liquidGlassPreset = GlassPreset.CUSTOM
```

### Exporting/Importing Settings

```kotlin
// Export all settings to JSON
val json = OvergramConfig.exportSettings()

// Import settings from JSON
OvergramConfig.importSettings(json)

// Reset to defaults
OvergramConfig.resetLiquidGlassSettings()
```

## Platform-Specific Features

### Android 12+ Native Blur

```kotlin
@RequiresApi(Build.VERSION_CODES.S)
fun setupNativeBlur(window: Window) {
    if (LiquidGlassHelper.supportsHardwareBlur()) {
        LiquidGlassHelper.applyWindowBlur(window, blurRadius = 25)
    }
}
```

### Checking Device Capabilities

```kotlin
val recommendedRadius = LiquidGlassHelper.getRecommendedBlurRadius(context)

when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        // Use native blur
        println("Native blur supported")
    }
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 -> {
        // Use RenderScript
        println("RenderScript blur")
    }
    else -> {
        // Software fallback
        println("Software blur only")
    }
}
```

## Best Practices

### 1. Memory Management

```kotlin
class MyActivity : AppCompatActivity() {
    private var glassEffect: LiquidGlassEffect? = null

    override fun onDestroy() {
        super.onDestroy()
        glassEffect?.destroy() // IMPORTANT: Clean up resources
    }
}
```

### 2. Cache Management

```kotlin
// Clear cache when theme changes
fun onThemeChanged() {
    glassEffect?.clearCache()
}

// Clear specific cache entry
fun onMessageDeleted(messageId: Long) {
    glassEffect?.clearCache("chat_bubble_$messageId")
}
```

### 3. Adaptive Quality

```kotlin
// Enable adaptive quality for better performance
val params = GlassParameters().apply {
    adaptiveQuality = true // Reduces quality during animations
}

// Notify when animating
recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        val isAnimating = newState != RecyclerView.SCROLL_STATE_IDLE
        glassBubble?.setAnimating(isAnimating)
    }
})
```

### 4. Background Capture Optimization

```kotlin
// Cache background captures
private var cachedBackground: Bitmap? = null
private var lastCaptureTime = 0L

private fun captureBackgroundOptimized(): Bitmap? {
    val now = System.currentTimeMillis()
    if (cachedBackground != null && (now - lastCaptureTime) < 100) {
        return cachedBackground
    }

    cachedBackground?.recycle()
    cachedBackground = captureBackground()
    lastCaptureTime = now

    return cachedBackground
}
```

## Troubleshooting

### Issue: Blur is too slow

**Solution:**
```kotlin
// Reduce blur radius
params.blurRadius = 10f

// Enable adaptive quality
params.adaptiveQuality = true

// Use native effects on Android 12+
OvergramConfig.liquidGlassUseNativeEffects = true
```

### Issue: High memory usage

**Solution:**
```kotlin
// Reduce max blur radius
params.maxBlurRadius = 25

// Clear cache more frequently
glassEffect.clearCache()

// Use smaller bitmap config
// (modify LiquidGlassEffect.kt to use RGB_565 instead of ARGB_8888)
```

### Issue: Visual artifacts

**Solution:**
```kotlin
// Ensure proper background capture
// Verify view hierarchy is correct
// Check bitmap format (should be ARGB_8888)

// Disable hardware acceleration if needed
params.useHardwareAcceleration = false
```

## Testing

### Unit Tests

```kotlin
@Test
fun testGlassParameters() {
    val params = GlassParameters.fromPreset(GlassPreset.STANDARD)
    assertEquals(20f, params.blurRadius)
    assertEquals(0.7f, params.backgroundOpacity)
}

@Test
fun testParameterValidation() {
    val invalid = GlassParameters(blurRadius = 1000f)
    val valid = LiquidGlassHelper.validateParameters(invalid)
    assertTrue(valid.blurRadius <= valid.maxBlurRadius)
}
```

### Performance Testing

```kotlin
@Test
fun testBlurPerformance() {
    val bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
    val effect = LiquidGlassEffect(context)

    val startTime = System.nanoTime()
    val blurred = effect.applyBlur(bitmap, 20f)
    val duration = (System.nanoTime() - startTime) / 1_000_000

    assertTrue("Blur took ${duration}ms", duration < 100)
}
```

## Additional Resources

- **Main Documentation**: See `LIQUID_GLASS_ANDROID.md`
- **Setup Guide**: See `OVERGRAM_ANDROID_SETUP_COMPLETE.md`
- **Source Code**: `TMessagesProj/src/main/java/one/overgram/messenger/ui/liquidglass/`
- **Settings**: `TMessagesProj/src/main/java/one/overgram/messenger/config/OvergramConfig.kt`

## Support

For issues or questions:
- **GitHub**: https://github.com/overspend1/Overgram4A/issues
- **Telegram**: https://t.me/overgramchat
- **Channel**: https://t.me/overgramupdates

---

**Liquid Glass** - Beautiful glassmorphism for Overgram Android
