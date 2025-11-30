package one.overgram.messenger.search

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/**
 * Advanced search filter with multiple criteria
 */
@Parcelize
data class AdvancedSearchFilter(
    // Text search
    val query: String = "",
    val useRegex: Boolean = false,
    val caseSensitive: Boolean = false,

    // Media filters
    val mediaType: MediaType = MediaType.ALL,
    val hasMedia: Boolean? = null, // null = any, true = with media, false = without media

    // Date filters
    val dateFrom: Long? = null,
    val dateTo: Long? = null,

    // Sender filters
    val fromUser: Long? = null, // User ID
    val fromChat: Long? = null, // Chat ID

    // File filters
    val minFileSize: Long? = null, // bytes
    val maxFileSize: Long? = null, // bytes
    val fileExtension: String? = null,

    // Message type filters
    val messageType: MessageType = MessageType.ALL,
    val hasLinks: Boolean? = null,
    val hasHashtags: Boolean? = null,
    val hasMentions: Boolean? = null,

    // Status filters
    val onlyEdited: Boolean = false,
    val onlyDeleted: Boolean = false,
    val onlyForwarded: Boolean = false,
    val onlyReplies: Boolean = false,
    val onlyPinned: Boolean = false,

    // Additional filters
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val language: String? = null
) : Parcelable {

    /**
     * Check if any filters are active
     */
    fun hasActiveFilters(): Boolean {
        return query.isNotBlank() ||
                mediaType != MediaType.ALL ||
                hasMedia != null ||
                dateFrom != null ||
                dateTo != null ||
                fromUser != null ||
                fromChat != null ||
                minFileSize != null ||
                maxFileSize != null ||
                fileExtension != null ||
                messageType != MessageType.ALL ||
                hasLinks != null ||
                hasHashtags != null ||
                hasMentions != null ||
                onlyEdited ||
                onlyDeleted ||
                onlyForwarded ||
                onlyReplies ||
                onlyPinned ||
                minLength != null ||
                maxLength != null ||
                language != null
    }

    /**
     * Get active filter count
     */
    fun getActiveFilterCount(): Int {
        var count = 0
        if (query.isNotBlank()) count++
        if (mediaType != MediaType.ALL) count++
        if (hasMedia != null) count++
        if (dateFrom != null || dateTo != null) count++
        if (fromUser != null || fromChat != null) count++
        if (minFileSize != null || maxFileSize != null) count++
        if (fileExtension != null) count++
        if (messageType != MessageType.ALL) count++
        if (hasLinks == true) count++
        if (hasHashtags == true) count++
        if (hasMentions == true) count++
        if (onlyEdited) count++
        if (onlyDeleted) count++
        if (onlyForwarded) count++
        if (onlyReplies) count++
        if (onlyPinned) count++
        if (minLength != null || maxLength != null) count++
        if (language != null) count++
        return count
    }

    /**
     * Validate regex pattern
     */
    fun isValidRegex(): Boolean {
        if (!useRegex || query.isBlank()) return true
        return try {
            Pattern.compile(query)
            true
        } catch (e: PatternSyntaxException) {
            false
        }
    }

    /**
     * Get compiled regex pattern
     */
    fun getRegexPattern(): Pattern? {
        if (!useRegex || query.isBlank()) return null
        return try {
            if (caseSensitive) {
                Pattern.compile(query)
            } else {
                Pattern.compile(query, Pattern.CASE_INSENSITIVE)
            }
        } catch (e: PatternSyntaxException) {
            null
        }
    }

    /**
     * Match text against query
     */
    fun matchesText(text: String): Boolean {
        if (query.isBlank()) return true

        return if (useRegex) {
            getRegexPattern()?.matcher(text)?.find() ?: false
        } else {
            if (caseSensitive) {
                text.contains(query)
            } else {
                text.contains(query, ignoreCase = true)
            }
        }
    }

    /**
     * Check if message length matches filter
     */
    fun matchesLength(length: Int): Boolean {
        if (minLength != null && length < minLength) return false
        if (maxLength != null && length > maxLength) return false
        return true
    }

    /**
     * Check if file size matches filter
     */
    fun matchesFileSize(size: Long): Boolean {
        if (minFileSize != null && size < minFileSize) return false
        if (maxFileSize != null && size > maxFileSize) return false
        return true
    }

    /**
     * Check if date matches filter
     */
    fun matchesDate(timestamp: Long): Boolean {
        if (dateFrom != null && timestamp < dateFrom) return false
        if (dateTo != null && timestamp > dateTo) return false
        return true
    }

    /**
     * Get summary text
     */
    fun getSummary(): String {
        return buildString {
            if (query.isNotBlank()) {
                append("\"$query\"")
                if (useRegex) append(" (regex)")
            }
            if (mediaType != MediaType.ALL) {
                if (isNotEmpty()) append(" • ")
                append(mediaType.name.lowercase())
            }
            if (dateFrom != null || dateTo != null) {
                if (isNotEmpty()) append(" • ")
                append("date filter")
            }
            if (getActiveFilterCount() > 3) {
                append(" + ${getActiveFilterCount() - 3} more")
            }
        }
    }

    /**
     * Clear all filters
     */
    fun clear(): AdvancedSearchFilter {
        return AdvancedSearchFilter()
    }
}

/**
 * Media type filter
 */
enum class MediaType {
    ALL,
    PHOTO,
    VIDEO,
    AUDIO,
    VOICE,
    FILE,
    GIF,
    STICKER,
    LOCATION,
    CONTACT
}

/**
 * Message type filter
 */
enum class MessageType {
    ALL,
    TEXT,
    MEDIA,
    SERVICE,
    POLL,
    QUIZ
}

/**
 * Search result with match details
 */
data class SearchResult(
    val messageId: Int,
    val chatId: Long,
    val text: String,
    val date: Int,
    val fromId: Long,
    val matchPositions: List<IntRange> = emptyList(),
    val score: Float = 1.0f
) {
    /**
     * Get highlighted text
     */
    fun getHighlightedText(): String {
        if (matchPositions.isEmpty()) return text

        val highlighted = StringBuilder()
        var lastEnd = 0

        matchPositions.forEach { range ->
            if (range.first > lastEnd) {
                highlighted.append(text.substring(lastEnd, range.first))
            }
            highlighted.append("<b>")
            highlighted.append(text.substring(range.first, minOf(range.last + 1, text.length)))
            highlighted.append("</b>")
            lastEnd = range.last + 1
        }

        if (lastEnd < text.length) {
            highlighted.append(text.substring(lastEnd))
        }

        return highlighted.toString()
    }
}

/**
 * Saved search query
 */
@Parcelize
data class SavedSearch(
    val id: Long = 0,
    val name: String,
    val filter: AdvancedSearchFilter,
    val resultCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsed: Long = System.currentTimeMillis()
) : Parcelable
