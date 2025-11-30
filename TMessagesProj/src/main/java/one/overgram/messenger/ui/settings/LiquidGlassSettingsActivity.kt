package one.overgram.messenger.ui.settings

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import one.overgram.messenger.config.OvergramConfig
import one.overgram.messenger.ui.liquidglass.GlassParameters
import one.overgram.messenger.ui.liquidglass.GlassPreset
import one.overgram.messenger.ui.liquidglass.LiquidGlassEffect
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.ActionBar
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Cells.HeaderCell
import org.telegram.ui.Cells.TextCheckCell
import org.telegram.ui.Cells.TextSettingsCell
import org.telegram.ui.Components.LayoutHelper
import org.telegram.ui.Components.SeekBarView

/**
 * Settings activity for Liquid Glass customization
 */
class LiquidGlassSettingsActivity : BaseFragment() {

    private lateinit var glassEffect: LiquidGlassEffect
    private lateinit var previewView: GlassPreviewView
    private var currentParams = GlassParameters()

    override fun createView(context: Context): View {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back)
        actionBar.setTitle("Liquid Glass")
        actionBar.setActionBarMenuOnItemClick(object : ActionBar.ActionBarMenuOnItemClick() {
            override fun onItemClick(id: Int) {
                if (id == -1) {
                    finishFragment()
                }
            }
        })

        // Initialize glass effect
        glassEffect = LiquidGlassEffect(context, currentParams)

        // Main layout
        val scrollView = ScrollView(context)
        val linearLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, AndroidUtilities.dp(8f), 0, AndroidUtilities.dp(8f))
        }

        // Preview section
        previewView = GlassPreviewView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                AndroidUtilities.dp(200f)
            ).apply {
                setMargins(
                    AndroidUtilities.dp(16f),
                    AndroidUtilities.dp(16f),
                    AndroidUtilities.dp(16f),
                    AndroidUtilities.dp(16f)
                )
            }
        }
        linearLayout.addView(previewView)

        // Enable/Disable switch
        addHeader(linearLayout, "General")
        addCheckCell(linearLayout, "Enable Liquid Glass", OvergramConfig.liquidGlassEnabled) { enabled ->
            OvergramConfig.liquidGlassEnabled = enabled
            updatePreview()
        }

        // Preset selection
        addHeader(linearLayout, "Presets")
        addPresetButtons(linearLayout)

        // Advanced settings
        addHeader(linearLayout, "Advanced")

        addSeekBar(linearLayout, "Blur Radius", 0f, 50f, currentParams.blurRadius) { value ->
            currentParams.blurRadius = value
            OvergramConfig.liquidGlassBlurRadius = value
            OvergramConfig.liquidGlassPreset = GlassPreset.CUSTOM
            updatePreview()
        }

        addSeekBar(linearLayout, "Opacity", 0f, 1f, currentParams.backgroundOpacity) { value ->
            currentParams.backgroundOpacity = value
            OvergramConfig.liquidGlassOpacity = value
            OvergramConfig.liquidGlassPreset = GlassPreset.CUSTOM
            updatePreview()
        }

        addSeekBar(linearLayout, "Saturation", 0f, 2f, currentParams.saturation) { value ->
            currentParams.saturation = value
            OvergramConfig.liquidGlassSaturation = value
            OvergramConfig.liquidGlassPreset = GlassPreset.CUSTOM
            updatePreview()
        }

        addSeekBar(linearLayout, "Brightness", 0f, 2f, currentParams.brightness) { value ->
            currentParams.brightness = value
            OvergramConfig.liquidGlassBrightness = value
            OvergramConfig.liquidGlassPreset = GlassPreset.CUSTOM
            updatePreview()
        }

        addSeekBar(linearLayout, "Border Opacity", 0f, 1f, currentParams.borderOpacity) { value ->
            currentParams.borderOpacity = value
            OvergramConfig.liquidGlassBorderOpacity = value
            updatePreview()
        }

        // Additional options
        addHeader(linearLayout, "Effects")

        addCheckCell(linearLayout, "Noise Texture", currentParams.useNoise) { enabled ->
            currentParams.useNoise = enabled
            OvergramConfig.liquidGlassUseNoise = enabled
            updatePreview()
        }

        if (currentParams.useNoise) {
            addSeekBar(linearLayout, "Noise Strength", 0f, 0.1f, currentParams.noiseStrength) { value ->
                currentParams.noiseStrength = value
                OvergramConfig.liquidGlassNoiseStrength = value
                updatePreview()
            }
        }

        // Application settings
        addHeader(linearLayout, "Apply To")

        addCheckCell(linearLayout, "Chat Bubbles", OvergramConfig.liquidGlassApplyToChatBubbles) { enabled ->
            OvergramConfig.liquidGlassApplyToChatBubbles = enabled
        }

        addCheckCell(linearLayout, "Fragments/Dialogs", OvergramConfig.liquidGlassApplyToFragments) { enabled ->
            OvergramConfig.liquidGlassApplyToFragments = enabled
        }

        // Performance settings
        addHeader(linearLayout, "Performance")

        if (LiquidGlassEffect.supportsNativeBlur()) {
            addCheckCell(linearLayout, "Use Native Blur (Android 12+)", OvergramConfig.liquidGlassUseNativeEffects) { enabled ->
                OvergramConfig.liquidGlassUseNativeEffects = enabled
            }
        }

        addCheckCell(linearLayout, "Adaptive Quality", currentParams.adaptiveQuality) { enabled ->
            currentParams.adaptiveQuality = enabled
            OvergramConfig.liquidGlassAdaptiveQuality = enabled
        }

        // Reset button
        val resetButton = TextView(context).apply {
            text = "Reset to Defaults"
            setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText))
            gravity = Gravity.CENTER
            setPadding(AndroidUtilities.dp(16f), AndroidUtilities.dp(16f), AndroidUtilities.dp(16f), AndroidUtilities.dp(16f))
            setOnClickListener {
                resetToDefaults()
            }
        }
        linearLayout.addView(resetButton)

        scrollView.addView(linearLayout)
        fragmentView = scrollView

        // Load current settings
        loadSettings()

        return fragmentView
    }

    private fun addHeader(layout: LinearLayout, text: String) {
        val header = HeaderCell(layout.context).apply {
            setText(text)
        }
        layout.addView(header, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT))
    }

    private fun addCheckCell(layout: LinearLayout, text: String, checked: Boolean, onChange: (Boolean) -> Unit) {
        val cell = TextCheckCell(layout.context).apply {
            setTextAndCheck(text, checked, true)
            setOnClickListener {
                val newValue = !isChecked
                setChecked(newValue)
                onChange(newValue)
            }
        }
        layout.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT))
    }

    private fun addSeekBar(layout: LinearLayout, label: String, min: Float, max: Float, current: Float, onChange: (Float) -> Unit) {
        val container = LinearLayout(layout.context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(AndroidUtilities.dp(16f), AndroidUtilities.dp(8f), AndroidUtilities.dp(16f), AndroidUtilities.dp(8f))
        }

        val labelView = TextView(layout.context).apply {
            text = "$label: ${String.format("%.2f", current)}"
            setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText))
            textSize = 14f
        }
        container.addView(labelView)

        val seekBar = SeekBarView(layout.context).apply {
            setProgress((current - min) / (max - min))
            setDelegate(object : SeekBarView.SeekBarViewDelegate {
                override fun onSeekBarDrag(stop: Boolean, progress: Float) {
                    val value = min + (max - min) * progress
                    labelView.text = "$label: ${String.format("%.2f", value)}"
                    onChange(value)
                }

                override fun onSeekBarPressed(pressed: Boolean) {}
            })
        }
        container.addView(seekBar, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 38))

        layout.addView(container)
    }

    private fun addPresetButtons(layout: LinearLayout) {
        val presets = GlassPreset.values().filter { it != GlassPreset.CUSTOM }
        val container = LinearLayout(layout.context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(AndroidUtilities.dp(16f), AndroidUtilities.dp(8f), AndroidUtilities.dp(16f), AndroidUtilities.dp(8f))
        }

        presets.forEach { preset ->
            val button = Button(layout.context).apply {
                text = preset.name.lowercase().capitalize()
                setOnClickListener {
                    applyPreset(preset)
                }
            }
            container.addView(button, LayoutHelper.createLinear(
                0,
                LayoutHelper.WRAP_CONTENT,
                1f,
                4, 0, 4, 0
            ))
        }

        layout.addView(container)
    }

    private fun applyPreset(preset: GlassPreset) {
        currentParams = GlassParameters.fromPreset(preset)
        OvergramConfig.liquidGlassPreset = preset

        // Save all parameters
        OvergramConfig.liquidGlassBlurRadius = currentParams.blurRadius
        OvergramConfig.liquidGlassOpacity = currentParams.backgroundOpacity
        OvergramConfig.liquidGlassSaturation = currentParams.saturation
        OvergramConfig.liquidGlassBrightness = currentParams.brightness
        OvergramConfig.liquidGlassBorderOpacity = currentParams.borderOpacity
        OvergramConfig.liquidGlassUseNoise = currentParams.useNoise
        OvergramConfig.liquidGlassNoiseStrength = currentParams.noiseStrength

        glassEffect.setParameters(currentParams)
        updatePreview()
    }

    private fun loadSettings() {
        val preset = OvergramConfig.liquidGlassPreset
        currentParams = GlassParameters.fromPreset(preset)

        // Override with custom values if preset is CUSTOM
        if (preset == GlassPreset.CUSTOM) {
            currentParams.blurRadius = OvergramConfig.liquidGlassBlurRadius
            currentParams.backgroundOpacity = OvergramConfig.liquidGlassOpacity
            currentParams.saturation = OvergramConfig.liquidGlassSaturation
            currentParams.brightness = OvergramConfig.liquidGlassBrightness
            currentParams.borderOpacity = OvergramConfig.liquidGlassBorderOpacity
            currentParams.useNoise = OvergramConfig.liquidGlassUseNoise
            currentParams.noiseStrength = OvergramConfig.liquidGlassNoiseStrength
        }

        glassEffect.setParameters(currentParams)
        updatePreview()
    }

    private fun resetToDefaults() {
        OvergramConfig.resetLiquidGlassSettings()
        loadSettings()
    }

    private fun updatePreview() {
        previewView.invalidate()
    }

    override fun onFragmentDestroy() {
        super.onFragmentDestroy()
        glassEffect.destroy()
    }

    /**
     * Preview view for glass effect
     */
    private inner class GlassPreviewView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = AndroidUtilities.dp(16f).toFloat()
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        private val backgroundBitmap: Bitmap by lazy {
            // Create a gradient background for preview
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val gradient = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(
                    Color.rgb(100, 150, 255),
                    Color.rgb(150, 100, 255),
                    Color.rgb(255, 100, 150)
                ),
                null,
                Shader.TileMode.CLAMP
            )
            paint.shader = gradient
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.shader = null
            bitmap
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            if (!OvergramConfig.liquidGlassEnabled) {
                // Show disabled state
                paint.color = Color.LTGRAY
                canvas.drawRoundRect(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    AndroidUtilities.dp(12f).toFloat(),
                    AndroidUtilities.dp(12f).toFloat(),
                    paint
                )
                canvas.drawText(
                    "Liquid Glass Disabled",
                    width / 2f,
                    height / 2f,
                    textPaint
                )
                return
            }

            // Draw glass effect on gradient background
            val bounds = RectF(
                AndroidUtilities.dp(32f).toFloat(),
                AndroidUtilities.dp(32f).toFloat(),
                width - AndroidUtilities.dp(32f).toFloat(),
                height - AndroidUtilities.dp(32f).toFloat()
            )

            if (width > 0 && height > 0 && !backgroundBitmap.isRecycled) {
                glassEffect.apply(canvas, bounds, backgroundBitmap)
            }

            // Draw preview text
            canvas.drawText(
                "Preview",
                width / 2f,
                height / 2f - AndroidUtilities.dp(8f),
                textPaint
            )
            canvas.drawText(
                currentParams.blurRadius.toInt().toString() + "dp blur",
                width / 2f,
                height / 2f + AndroidUtilities.dp(16f),
                textPaint.apply { textSize = AndroidUtilities.dp(12f).toFloat() }
            )
        }
    }
}
