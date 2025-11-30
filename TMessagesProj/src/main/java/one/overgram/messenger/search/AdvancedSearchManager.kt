package one.overgram.messenger.search

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.*
import org.telegram.messenger.MessagesController
import org.telegram.messenger.MessagesStorage
import org.telegram.tgnet.TLRPC
import java.util.*

/**
 * Manager for advanced search functionality
 *
 * Features:
 * - Complex search queries with multiple filters
 * - Save and manage search queries
 * - Search across all chats or specific chats
 * - Regular expression support
 * - Performance optimized with coroutines
 */
class AdvancedSearchManager private constructor(private val context: Context) {

    private val dbHelper = SearchDatabaseHelper(context)
    private val messagesStorage = MessagesStorage.getInstance(0)
    private val messagesController = MessagesController.getInstance(0)

    private val searchScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        @Volatile
        private var instance: AdvancedSearchManager? = null

        fun getInstance(context: Context): AdvancedSearchManager {
            return instance ?: synchronized(this) {
                instance ?: AdvancedSearchManager(context.applicationContext).also { instance = it }
            }
        }

        private const val MAX_RESULTS = 1000
        private const val BATCH_SIZE = 100
    }

    /**
     * Perform advanced search
     */
    suspend fun search(
        filter: AdvancedSearchFilter,
        dialogId: Long? = null,
        limit: Int = MAX_RESULTS,
        onProgress: ((Int) -> Unit)? = null
    ): List<SearchResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<SearchResult>()

        if (!filter.hasActiveFilters()) {
            return@withContext emptyList()
        }

        // Validate regex if used
        if (filter.useRegex && !filter.isValidRegex()) {
            return@withContext emptyList()
        }

        try {
            if (dialogId != null) {
                // Search in specific dialog
                searchInDialog(dialogId, filter, results, limit, onProgress)
            } else {
                // Search across all dialogs
                searchAllDialogs(filter, results, limit, onProgress)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Sort by score and date
        results.sortedByDescending { it.score * it.date }
            .take(limit)
    }

    /**
     * Search in specific dialog
     */
    private suspend fun searchInDialog(
        dialogId: Long,
        filter: AdvancedSearchFilter,
        results: MutableList<SearchResult>,
        limit: Int,
        onProgress: ((Int) -> Unit)?
    ) {
        val db = messagesStorage.database ?: return

        val query = buildSearchQuery(filter, dialogId)
        val cursor = db.rawQuery(query, null)

        cursor.use {
            var processed = 0
            while (cursor.moveToNext() && results.size < limit) {
                val messageId = cursor.getInt(cursor.getColumnIndex("mid"))
                val messageData = cursor.getBlob(cursor.getColumnIndex("data"))
                val date = cursor.getInt(cursor.getColumnIndex("date"))

                // Deserialize message
                val message = try {
                    TLRPC.Message.TLdeserialize(
                        org.telegram.tgnet.AbstractSerializedData.getInstance(messageData),
                        messageData[0].toInt(),
                        false
                    )
                } catch (e: Exception) {
                    null
                }

                message?.let {
                    if (matchesFilter(it, filter)) {
                        val result = createSearchResult(it, filter)
                        if (result != null) {
                            results.add(result)
                        }
                    }
                }

                processed++
                if (processed % BATCH_SIZE == 0) {
                    onProgress?.invoke(processed)
                    delay(10) // Prevent UI freeze
                }
            }
        }
    }

    /**
     * Search across all dialogs
     */
    private suspend fun searchAllDialogs(
        filter: AdvancedSearchFilter,
        results: MutableList<SearchResult>,
        limit: Int,
        onProgress: ((Int) -> Unit)?
    ) {
        // Get all dialog IDs
        val dialogs = messagesController.allDialogs
        var totalProcessed = 0

        for (dialog in dialogs) {
            if (results.size >= limit) break

            searchInDialog(
                dialogId = dialog.id,
                filter = filter,
                results = results,
                limit = limit - results.size,
                onProgress = { count ->
                    totalProcessed += count
                    onProgress?.invoke(totalProcessed)
                }
            )
        }
    }

    /**
     * Build SQL query based on filter
     */
    private fun buildSearchQuery(filter: AdvancedSearchFilter, dialogId: Long): String {
        return buildString {
            append("SELECT mid, data, date FROM messages WHERE uid = $dialogId")

            // Date filter
            if (filter.dateFrom != null) {
                append(" AND date >= ${filter.dateFrom / 1000}")
            }
            if (filter.dateTo != null) {
                append(" AND date <= ${filter.dateTo / 1000}")
            }

            // Sender filter
            if (filter.fromUser != null) {
                append(" AND send_state = 0 AND from_id = ${filter.fromUser}")
            }

            append(" ORDER BY date DESC")
        }
    }

    /**
     * Check if message matches all filter criteria
     */
    private fun matchesFilter(message: TLRPC.Message, filter: AdvancedSearchFilter): Boolean {
        // Text search
        val messageText = message.message ?: ""
        if (!filter.matchesText(messageText)) {
            return false
        }

        // Message length
        if (!filter.matchesLength(messageText.length)) {
            return false
        }

        // Date filter
        if (!filter.matchesDate(message.date.toLong() * 1000)) {
            return false
        }

        // Media filters
        if (filter.hasMedia != null) {
            val hasMedia = message.media != null && message.media !is TLRPC.TL_messageMediaEmpty
            if (filter.hasMedia != hasMedia) {
                return false
            }
        }

        if (filter.mediaType != MediaType.ALL) {
            if (!matchesMediaType(message, filter.mediaType)) {
                return false
            }
        }

        // File size filter
        if (filter.minFileSize != null || filter.maxFileSize != null) {
            val fileSize = getMessageFileSize(message)
            if (fileSize != null && !filter.matchesFileSize(fileSize)) {
                return false
            }
        }

        // File extension filter
        if (filter.fileExtension != null) {
            val fileName = getMessageFileName(message)
            if (fileName == null || !fileName.endsWith(filter.fileExtension, ignoreCase = true)) {
                return false
            }
        }

        // Message type filters
        if (filter.messageType != MessageType.ALL) {
            if (!matchesMessageType(message, filter.messageType)) {
                return false
            }
        }

        // Status filters
        if (filter.onlyEdited && message.edit_date == 0) {
            return false
        }

        if (filter.onlyForwarded && message.fwd_from == null) {
            return false
        }

        if (filter.onlyReplies && message.reply_to == null) {
            return false
        }

        // Content filters
        if (filter.hasLinks == true && !containsLinks(messageText)) {
            return false
        }

        if (filter.hasHashtags == true && !containsHashtags(messageText)) {
            return false
        }

        if (filter.hasMentions == true && !containsMentions(messageText)) {
            return false
        }

        return true
    }

    /**
     * Check if message matches media type filter
     */
    private fun matchesMediaType(message: TLRPC.Message, mediaType: MediaType): Boolean {
        val media = message.media ?: return false

        return when (mediaType) {
            MediaType.PHOTO -> media is TLRPC.TL_messageMediaPhoto
            MediaType.VIDEO -> media is TLRPC.TL_messageMediaDocument &&
                    media.document?.mime_type?.startsWith("video/") == true
            MediaType.AUDIO -> media is TLRPC.TL_messageMediaDocument &&
                    media.document?.mime_type?.startsWith("audio/") == true
            MediaType.VOICE -> media is TLRPC.TL_messageMediaDocument &&
                    media.document?.attributes?.any { it is TLRPC.TL_documentAttributeAudio && it.voice } == true
            MediaType.FILE -> media is TLRPC.TL_messageMediaDocument
            MediaType.GIF -> media is TLRPC.TL_messageMediaDocument &&
                    media.document?.mime_type == "image/gif"
            MediaType.STICKER -> media is TLRPC.TL_messageMediaDocument &&
                    media.document?.attributes?.any { it is TLRPC.TL_documentAttributeSticker } == true
            MediaType.LOCATION -> media is TLRPC.TL_messageMediaGeo || media is TLRPC.TL_messageMediaVenue
            MediaType.CONTACT -> media is TLRPC.TL_messageMediaContact
            else -> false
        }
    }

    /**
     * Check if message matches message type filter
     */
    private fun matchesMessageType(message: TLRPC.Message, messageType: MessageType): Boolean {
        return when (messageType) {
            MessageType.TEXT -> message.message?.isNotEmpty() == true && message.media is TLRPC.TL_messageMediaEmpty
            MessageType.MEDIA -> message.media != null && message.media !is TLRPC.TL_messageMediaEmpty
            MessageType.SERVICE -> message is TLRPC.TL_messageService
            MessageType.POLL -> message.media is TLRPC.TL_messageMediaPoll
            MessageType.QUIZ -> message.media is TLRPC.TL_messageMediaPoll &&
                    (message.media as TLRPC.TL_messageMediaPoll).poll.quiz
            else -> true
        }
    }

    /**
     * Create search result from message
     */
    private fun createSearchResult(message: TLRPC.Message, filter: AdvancedSearchFilter): SearchResult? {
        val text = message.message ?: return null
        val matchPositions = if (filter.query.isNotEmpty()) {
            findMatchPositions(text, filter)
        } else {
            emptyList()
        }

        val score = calculateRelevanceScore(message, filter, matchPositions)

        return SearchResult(
            messageId = message.id,
            chatId = message.dialog_id,
            text = text,
            date = message.date,
            fromId = message.from_id?.user_id ?: 0,
            matchPositions = matchPositions,
            score = score
        )
    }

    /**
     * Find match positions in text
     */
    private fun findMatchPositions(text: String, filter: AdvancedSearchFilter): List<IntRange> {
        val positions = mutableListOf<IntRange>()

        if (filter.useRegex) {
            val pattern = filter.getRegexPattern() ?: return emptyList()
            val matcher = pattern.matcher(text)
            while (matcher.find()) {
                positions.add(matcher.start()..matcher.end() - 1)
            }
        } else {
            var startIndex = 0
            while (true) {
                val index = text.indexOf(filter.query, startIndex, ignoreCase = !filter.caseSensitive)
                if (index == -1) break
                positions.add(index until (index + filter.query.length))
                startIndex = index + 1
            }
        }

        return positions
    }

    /**
     * Calculate relevance score for ranking
     */
    private fun calculateRelevanceScore(
        message: TLRPC.Message,
        filter: AdvancedSearchFilter,
        matchPositions: List<IntRange>
    ): Float {
        var score = 1.0f

        // More matches = higher score
        score += matchPositions.size * 0.1f

        // Recent messages score higher
        val daysSinceMessage = (System.currentTimeMillis() / 1000 - message.date) / 86400
        score *= (1.0f / (1.0f + daysSinceMessage * 0.01f))

        // Exact matches score higher
        if (!filter.useRegex && matchPositions.size == 1) {
            val matchLength = matchPositions[0].last - matchPositions[0].first + 1
            if (matchLength == filter.query.length) {
                score *= 1.5f
            }
        }

        return score
    }

    /**
     * Helper functions
     */
    private fun getMessageFileSize(message: TLRPC.Message): Long? {
        val document = (message.media as? TLRPC.TL_messageMediaDocument)?.document
        return document?.size?.toLong()
    }

    private fun getMessageFileName(message: TLRPC.Message): String? {
        val document = (message.media as? TLRPC.TL_messageMediaDocument)?.document
        return document?.attributes?.firstOrNull { it is TLRPC.TL_documentAttributeFilename }
            ?.let { (it as TLRPC.TL_documentAttributeFilename).file_name }
    }

    private fun containsLinks(text: String): Boolean {
        return text.contains("http://", ignoreCase = true) ||
                text.contains("https://", ignoreCase = true) ||
                text.contains("www.", ignoreCase = true)
    }

    private fun containsHashtags(text: String): Boolean {
        return text.contains(Regex("#\\w+"))
    }

    private fun containsMentions(text: String): Boolean {
        return text.contains(Regex("@\\w+"))
    }

    /**
     * Save search query
     */
    fun saveSearch(savedSearch: SavedSearch): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", savedSearch.name)
            put("filter_data", serializeFilter(savedSearch.filter))
            put("result_count", savedSearch.resultCount)
            put("created_at", savedSearch.createdAt)
            put("last_used", savedSearch.lastUsed)
        }

        return if (savedSearch.id == 0L) {
            db.insert("saved_searches", null, values)
        } else {
            db.update("saved_searches", values, "id = ?", arrayOf(savedSearch.id.toString()))
            savedSearch.id
        }
    }

    /**
     * Get all saved searches
     */
    fun getSavedSearches(): List<SavedSearch> {
        val searches = mutableListOf<SavedSearch>()
        val db = dbHelper.readableDatabase

        db.query("saved_searches", null, null, null, null, null, "last_used DESC").use { cursor ->
            while (cursor.moveToNext()) {
                searches.add(cursorToSavedSearch(cursor))
            }
        }

        return searches
    }

    /**
     * Delete saved search
     */
    fun deleteSavedSearch(id: Long): Boolean {
        val db = dbHelper.writableDatabase
        return db.delete("saved_searches", "id = ?", arrayOf(id.toString())) > 0
    }

    private fun serializeFilter(filter: AdvancedSearchFilter): String {
        // TODO: Implement JSON serialization
        return ""
    }

    private fun deserializeFilter(data: String): AdvancedSearchFilter {
        // TODO: Implement JSON deserialization
        return AdvancedSearchFilter()
    }

    private fun cursorToSavedSearch(cursor: android.database.Cursor): SavedSearch {
        return SavedSearch(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
            filter = deserializeFilter(cursor.getString(cursor.getColumnIndexOrThrow("filter_data"))),
            resultCount = cursor.getInt(cursor.getColumnIndexOrThrow("result_count")),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at")),
            lastUsed = cursor.getLong(cursor.getColumnIndexOrThrow("last_used"))
        )
    }

    fun cleanup() {
        searchScope.cancel()
    }
}

/**
 * Database helper for saved searches
 */
private class SearchDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "overgram_search.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE saved_searches (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                filter_data TEXT NOT NULL,
                result_count INTEGER DEFAULT 0,
                created_at INTEGER NOT NULL,
                last_used INTEGER NOT NULL
            )
        """)

        db.execSQL("CREATE INDEX idx_last_used ON saved_searches(last_used)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Future migrations
    }
}
