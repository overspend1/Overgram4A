package one.overgram.messenger.templates

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.regex.Pattern

/**
 * Message template with variable support
 *
 * Variables:
 * - {name} - Contact name
 * - {date} - Current date
 * - {time} - Current time
 * - {day} - Day of week
 * - {custom} - User-defined variables
 */
@Parcelize
data class MessageTemplate(
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "General",
    val useCount: Int = 0,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable {

    companion object {
        // Variable pattern: {variable_name}
        private val VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)\\}")

        // Built-in variables
        const val VAR_NAME = "name"
        const val VAR_DATE = "date"
        const val VAR_TIME = "time"
        const val VAR_DAY = "day"
        const val VAR_DATETIME = "datetime"
        const val VAR_FIRSTNAME = "firstname"
        const val VAR_LASTNAME = "lastname"
        const val VAR_USERNAME = "username"
    }

    /**
     * Extract all variables from template content
     */
    fun getVariables(): List<String> {
        val variables = mutableListOf<String>()
        val matcher = VARIABLE_PATTERN.matcher(content)
        while (matcher.find()) {
            matcher.group(1)?.let { variables.add(it) }
        }
        return variables.distinct()
    }

    /**
     * Check if template has variables
     */
    fun hasVariables(): Boolean = VARIABLE_PATTERN.matcher(content).find()

    /**
     * Check if template has custom (non-built-in) variables
     */
    fun hasCustomVariables(): Boolean {
        val vars = getVariables()
        return vars.any { !isBuiltInVariable(it) }
    }

    /**
     * Check if a variable is built-in
     */
    private fun isBuiltInVariable(variable: String): Boolean {
        return when (variable.lowercase()) {
            VAR_NAME, VAR_DATE, VAR_TIME, VAR_DAY, VAR_DATETIME,
            VAR_FIRSTNAME, VAR_LASTNAME, VAR_USERNAME -> true
            else -> false
        }
    }

    /**
     * Get preview text (first 50 chars)
     */
    fun getPreview(): String {
        val preview = content.replace("\n", " ").trim()
        return if (preview.length > 50) {
            preview.substring(0, 50) + "..."
        } else {
            preview
        }
    }

    /**
     * Validate template
     */
    fun isValid(): Boolean {
        return title.isNotBlank() && content.isNotBlank()
    }

    /**
     * Copy template with incremented use count
     */
    fun incrementUseCount(): MessageTemplate {
        return copy(useCount = useCount + 1, updatedAt = System.currentTimeMillis())
    }

    /**
     * Toggle favorite status
     */
    fun toggleFavorite(): MessageTemplate {
        return copy(isFavorite = !isFavorite, updatedAt = System.currentTimeMillis())
    }
}

/**
 * Template category
 */
data class TemplateCategory(
    val name: String,
    val icon: String = "📁",
    val color: Int = 0xFF2196F3.toInt(),
    val templateCount: Int = 0
) {
    companion object {
        val DEFAULT_CATEGORIES = listOf(
            TemplateCategory("General", "📁", 0xFF2196F3.toInt()),
            TemplateCategory("Work", "💼", 0xFF4CAF50.toInt()),
            TemplateCategory("Personal", "💬", 0xFF9C27B0.toInt()),
            TemplateCategory("Business", "📊", 0xFFFF9800.toInt()),
            TemplateCategory("Greetings", "👋", 0xFFF44336.toInt()),
            TemplateCategory("Responses", "💭", 0xFF00BCD4.toInt())
        )
    }
}

/**
 * Variable replacement result
 */
data class TemplateResult(
    val text: String,
    val unresolvedVariables: List<String> = emptyList()
) {
    fun isComplete() = unresolvedVariables.isEmpty()
}
