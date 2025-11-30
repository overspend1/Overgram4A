package one.overgram.messenger.config

import android.content.Context
import android.content.SharedPreferences
import one.overgram.messenger.ui.liquidglass.GlassPreset
import org.telegram.messenger.ApplicationLoader

/**
 * Overgram configuration and settings
 */
object OvergramConfig {
    private const val PREFS_NAME = "overgramconfig"

    private val prefs: SharedPreferences by lazy {
        ApplicationLoader.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ========================================
    // Liquid Glass Settings
    // ========================================

    var liquidGlassEnabled: Boolean
        get() = prefs.getBoolean("liquidGlassEnabled", false)
        set(value) = prefs.edit().putBoolean("liquidGlassEnabled", value).apply()

    var liquidGlassPreset: GlassPreset
        get() = GlassPreset.values()[prefs.getInt("liquidGlassPreset", GlassPreset.STANDARD.ordinal)]
        set(value) = prefs.edit().putInt("liquidGlassPreset", value.ordinal).apply()

    var liquidGlassBlurRadius: Float
        get() = prefs.getFloat("liquidGlassBlurRadius", 20f)
        set(value) = prefs.edit().putFloat("liquidGlassBlurRadius", value).apply()

    var liquidGlassOpacity: Float
        get() = prefs.getFloat("liquidGlassOpacity", 0.7f)
        set(value) = prefs.edit().putFloat("liquidGlassOpacity", value).apply()

    var liquidGlassSaturation: Float
        get() = prefs.getFloat("liquidGlassSaturation", 1.2f)
        set(value) = prefs.edit().putFloat("liquidGlassSaturation", value).apply()

    var liquidGlassBrightness: Float
        get() = prefs.getFloat("liquidGlassBrightness", 1.05f)
        set(value) = prefs.edit().putFloat("liquidGlassBrightness", value).apply()

    var liquidGlassTintColor: Int
        get() = prefs.getInt("liquidGlassTintColor", 0x19FFFFFF)
        set(value) = prefs.edit().putInt("liquidGlassTintColor", value).apply()

    var liquidGlassBorderOpacity: Float
        get() = prefs.getFloat("liquidGlassBorderOpacity", 0.3f)
        set(value) = prefs.edit().putFloat("liquidGlassBorderOpacity", value).apply()

    var liquidGlassUseNoise: Boolean
        get() = prefs.getBoolean("liquidGlassUseNoise", true)
        set(value) = prefs.edit().putBoolean("liquidGlassUseNoise", value).apply()

    var liquidGlassNoiseStrength: Float
        get() = prefs.getFloat("liquidGlassNoiseStrength", 0.02f)
        set(value) = prefs.edit().putFloat("liquidGlassNoiseStrength", value).apply()

    var liquidGlassApplyToChatBubbles: Boolean
        get() = prefs.getBoolean("liquidGlassApplyToChatBubbles", true)
        set(value) = prefs.edit().putBoolean("liquidGlassApplyToChatBubbles", value).apply()

    var liquidGlassApplyToFragments: Boolean
        get() = prefs.getBoolean("liquidGlassApplyToFragments", false)
        set(value) = prefs.edit().putBoolean("liquidGlassApplyToFragments", value).apply()

    var liquidGlassUseNativeEffects: Boolean
        get() = prefs.getBoolean("liquidGlassUseNativeEffects", true)
        set(value) = prefs.edit().putBoolean("liquidGlassUseNativeEffects", value).apply()

    var liquidGlassAdaptiveQuality: Boolean
        get() = prefs.getBoolean("liquidGlassAdaptiveQuality", true)
        set(value) = prefs.edit().putBoolean("liquidGlassAdaptiveQuality", value).apply()

    var liquidGlassMaxBlurRadius: Int
        get() = prefs.getInt("liquidGlassMaxBlurRadius", 50)
        set(value) = prefs.edit().putInt("liquidGlassMaxBlurRadius", value).apply()

    // ========================================
    // Ghost Mode Settings
    // ========================================

    var ghostModeEnabled: Boolean
        get() = prefs.getBoolean("ghostModeEnabled", false)
        set(value) = prefs.edit().putBoolean("ghostModeEnabled", value).apply()

    var hideOnlineStatus: Boolean
        get() = prefs.getBoolean("hideOnlineStatus", false)
        set(value) = prefs.edit().putBoolean("hideOnlineStatus", value).apply()

    var hideReadReceipts: Boolean
        get() = prefs.getBoolean("hideReadReceipts", false)
        set(value) = prefs.edit().putBoolean("hideReadReceipts", value).apply()

    var hideTypingIndicator: Boolean
        get() = prefs.getBoolean("hideTypingIndicator", false)
        set(value) = prefs.edit().putBoolean("hideTypingIndicator", value).apply()

    var hideVoiceRecording: Boolean
        get() = prefs.getBoolean("hideVoiceRecording", false)
        set(value) = prefs.edit().putBoolean("hideVoiceRecording", value).apply()

    // ========================================
    // Message History Settings
    // ========================================

    var messageHistoryEnabled: Boolean
        get() = prefs.getBoolean("messageHistoryEnabled", true)
        set(value) = prefs.edit().putBoolean("messageHistoryEnabled", value).apply()

    var saveDeletedMessages: Boolean
        get() = prefs.getBoolean("saveDeletedMessages", true)
        set(value) = prefs.edit().putBoolean("saveDeletedMessages", value).apply()

    var saveEditedMessages: Boolean
        get() = prefs.getBoolean("saveEditedMessages", true)
        set(value) = prefs.edit().putBoolean("saveEditedMessages", value).apply()

    var messageHistoryLimit: Int
        get() = prefs.getInt("messageHistoryLimit", 1000)
        set(value) = prefs.edit().putInt("messageHistoryLimit", value).apply()

    // ========================================
    // Appearance Settings
    // ========================================

    var useSystemFont: Boolean
        get() = prefs.getBoolean("useSystemFont", false)
        set(value) = prefs.edit().putBoolean("useSystemFont", value).apply()

    var customFontPath: String
        get() = prefs.getString("customFontPath", "") ?: ""
        set(value) = prefs.edit().putString("customFontPath", value).apply()

    var chatBubbleStyle: Int
        get() = prefs.getInt("chatBubbleStyle", 0)
        set(value) = prefs.edit().putInt("chatBubbleStyle", value).apply()

    var showDeletedMark: Boolean
        get() = prefs.getBoolean("showDeletedMark", true)
        set(value) = prefs.edit().putBoolean("showDeletedMark", value).apply()

    var showEditedMark: Boolean
        get() = prefs.getBoolean("showEditedMark", true)
        set(value) = prefs.edit().putBoolean("showEditedMark", value).apply()

    // ========================================
    // Premium Settings
    // ========================================

    var localPremiumEnabled: Boolean
        get() = prefs.getBoolean("localPremiumEnabled", false)
        set(value) = prefs.edit().putBoolean("localPremiumEnabled", value).apply()

    var unlockAllStickers: Boolean
        get() = prefs.getBoolean("unlockAllStickers", false)
        set(value) = prefs.edit().putBoolean("unlockAllStickers", value).apply()

    var increaseUploadLimit: Boolean
        get() = prefs.getBoolean("increaseUploadLimit", false)
        set(value) = prefs.edit().putBoolean("increaseUploadLimit", value).apply()

    // ========================================
    // Privacy Settings
    // ========================================

    var allowScreenshotsInSecretChats: Boolean
        get() = prefs.getBoolean("allowScreenshotsInSecretChats", false)
        set(value) = prefs.edit().putBoolean("allowScreenshotsInSecretChats", value).apply()

    var disableEmulatorDetection: Boolean
        get() = prefs.getBoolean("disableEmulatorDetection", false)
        set(value) = prefs.edit().putBoolean("disableEmulatorDetection", value).apply()

    var bypassTTLRestrictions: Boolean
        get() = prefs.getBoolean("bypassTTLRestrictions", false)
        set(value) = prefs.edit().putBoolean("bypassTTLRestrictions", value).apply()

    // ========================================
    // Filter Settings
    // ========================================

    var messageFiltersEnabled: Boolean
        get() = prefs.getBoolean("messageFiltersEnabled", false)
        set(value) = prefs.edit().putBoolean("messageFiltersEnabled", value).apply()

    var filterSpam: Boolean
        get() = prefs.getBoolean("filterSpam", false)
        set(value) = prefs.edit().putBoolean("filterSpam", value).apply()

    var filterForwards: Boolean
        get() = prefs.getBoolean("filterForwards", false)
        set(value) = prefs.edit().putBoolean("filterForwards", value).apply()

    // ========================================
    // Utility Functions
    // ========================================

    /**
     * Reset all settings to defaults
     */
    fun resetToDefaults() {
        prefs.edit().clear().apply()
    }

    /**
     * Reset liquid glass settings only
     */
    fun resetLiquidGlassSettings() {
        prefs.edit().apply {
            remove("liquidGlassEnabled")
            remove("liquidGlassPreset")
            remove("liquidGlassBlurRadius")
            remove("liquidGlassOpacity")
            remove("liquidGlassSaturation")
            remove("liquidGlassBrightness")
            remove("liquidGlassTintColor")
            remove("liquidGlassBorderOpacity")
            remove("liquidGlassUseNoise")
            remove("liquidGlassNoiseStrength")
            remove("liquidGlassApplyToChatBubbles")
            remove("liquidGlassApplyToFragments")
            remove("liquidGlassUseNativeEffects")
            remove("liquidGlassAdaptiveQuality")
            remove("liquidGlassMaxBlurRadius")
            apply()
        }
    }

    /**
     * Export settings to JSON string
     */
    fun exportSettings(): String {
        val allSettings = prefs.all
        return buildString {
            append("{\n")
            allSettings.entries.forEachIndexed { index, entry ->
                append("  \"${entry.key}\": ")
                when (val value = entry.value) {
                    is String -> append("\"$value\"")
                    is Boolean -> append(value)
                    is Int -> append(value)
                    is Float -> append(value)
                    else -> append("null")
                }
                if (index < allSettings.size - 1) append(",")
                append("\n")
            }
            append("}")
        }
    }

    /**
     * Import settings from JSON string
     */
    fun importSettings(json: String) {
        // Basic JSON parsing (consider using a proper JSON library in production)
        val editor = prefs.edit()

        json.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("\"") && trimmed.contains(":")) {
                val parts = trimmed.split(":", limit = 2)
                if (parts.size == 2) {
                    val key = parts[0].trim().removeSurrounding("\"")
                    val value = parts[1].trim().removeSuffix(",")

                    when {
                        value == "true" -> editor.putBoolean(key, true)
                        value == "false" -> editor.putBoolean(key, false)
                        value.startsWith("\"") -> editor.putString(key, value.removeSurrounding("\""))
                        value.contains(".") -> editor.putFloat(key, value.toFloatOrNull() ?: 0f)
                        else -> editor.putInt(key, value.toIntOrNull() ?: 0)
                    }
                }
            }
        }

        editor.apply()
    }
}
