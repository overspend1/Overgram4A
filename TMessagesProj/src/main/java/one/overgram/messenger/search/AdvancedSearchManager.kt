package one.overgram.messenger.search

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase as AndroidSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.telegram.SQLite.SQLiteCursor
import org.telegram.SQLite.SQLiteDatabase
import org.telegram.messenger.MessagesStorage
import org.telegram.tgnet.SerializedData
import org.telegram.tgnet.TLRPC

/**
 * Lightweight stub for advanced search.
 * Returns empty results for now but keeps saved-search management intact.
 */
class AdvancedSearchManager private constructor(@Suppress("unused") private val context: Context) {

    private val dbHelper = SearchDatabaseHelper(context)

    companion object {
        @Volatile
        private var instance: AdvancedSearchManager? = null

        fun getInstance(context: Context): AdvancedSearchManager {
            return instance ?: synchronized(this) {
                instance ?: AdvancedSearchManager(context.applicationContext).also { instance = it }
            }
        }
    }

    suspend fun search(
        filter: AdvancedSearchFilter,
        dialogId: Long? = null,
        limit: Int = 0,
        onProgress: ((Int) -> Unit)? = null
    ): List<SearchResult> = withContext(Dispatchers.IO) {
        if (!filter.hasActiveFilters()) {
            onProgress?.invoke(0)
            return@withContext emptyList()
        }

        val results = mutableListOf<SearchResult>()

        if (dialogId != null) {
            results.addAll(searchInDialog(dialogId, filter, if (limit > 0) limit else 300, onProgress))
        } else {
            // Without a specific dialog, return empty for now to avoid heavy DB scans.
            onProgress?.invoke(0)
        }

        results.sortedByDescending { it.score }
    }

    fun saveSearch(savedSearch: SavedSearch): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", savedSearch.name)
            put("filter_data", "") // stub storage
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

    fun getSavedSearches(): List<SavedSearch> {
        val searches = mutableListOf<SavedSearch>()
        val db = dbHelper.readableDatabase

        db.query("saved_searches", null, null, null, null, null, "last_used DESC").use { cursor ->
            while (cursor.moveToNext()) {
                searches.add(
                    SavedSearch(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        filter = AdvancedSearchFilter(),
                        resultCount = cursor.getInt(cursor.getColumnIndexOrThrow("result_count")),
                        createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at")),
                        lastUsed = cursor.getLong(cursor.getColumnIndexOrThrow("last_used"))
                    )
                )
            }
        }

        return searches
    }

    fun deleteSavedSearch(id: Long): Boolean {
        val db = dbHelper.writableDatabase
        return db.delete("saved_searches", "id = ?", arrayOf(id.toString())) > 0
    }

    fun cleanup() {
        // no-op for stub
    }

    private fun searchInDialog(
        dialogId: Long,
        filter: AdvancedSearchFilter,
        limit: Int,
        onProgress: ((Int) -> Unit)?
    ): List<SearchResult> {
        val db = MessagesStorage.getInstance(0).database ?: return emptyList()
        val messages = loadMessages(db, dialogId, limit)
        val results = mutableListOf<SearchResult>()

        for ((index, msg) in messages.withIndex()) {
            val text = msg.message ?: ""
            if (!filter.matchesText(text)) continue
            if (!filter.matchesLength(text.length)) continue
            if (!filter.matchesDate(msg.date.toLong() * 1000)) continue

            val matchPositions = findMatchPositions(text, filter)
            val score = 1f + matchPositions.size * 0.1f

            results.add(
                SearchResult(
                    messageId = msg.id,
                    chatId = dialogId,
                    text = text,
                    date = msg.date,
                    fromId = msg.from_id?.user_id?.toLong() ?: 0L,
                    matchPositions = matchPositions,
                    score = score
                )
            )

            if (results.size >= limit) break
            if (index % 50 == 0) {
                onProgress?.invoke(index)
            }
        }

        return results
    }

    private fun loadMessages(db: SQLiteDatabase, dialogId: Long, limit: Int): List<TLRPC.Message> {
        val messages = mutableListOf<TLRPC.Message>()

        fun query(table: String): Boolean {
            return try {
                val cursor: SQLiteCursor = db.queryFinalized("SELECT mid, data, date FROM $table WHERE uid = ? ORDER BY date DESC LIMIT ?", dialogId, limit)
                while (cursor.next()) {
                    val data = cursor.byteArrayValue(1) ?: continue
                    val stream = SerializedData(data)
                    val message = TLRPC.Message.TLdeserialize(stream, stream.readInt32(false), false)
                    if (message != null) {
                        message.id = cursor.intValue(0)
                        message.date = cursor.intValue(2)
                        messages.add(message)
                    }
                }
                cursor.dispose()
                true
            } catch (e: Exception) {
                false
            }
        }

        // Try modern table first, then fallback
        if (!query("messages_v2")) {
            query("messages")
        }

        return messages
    }

    private fun findMatchPositions(text: String, filter: AdvancedSearchFilter): List<IntRange> {
        if (filter.query.isBlank()) return emptyList()
        val positions = mutableListOf<IntRange>()

        if (filter.useRegex) {
            val pattern = filter.getRegexPattern() ?: return emptyList()
            val matcher = pattern.matcher(text)
            while (matcher.find()) {
                positions.add(matcher.start()..(matcher.end() - 1))
            }
        } else {
            var startIndex = 0
            val needle = filter.query
            while (true) {
                val idx = text.indexOf(needle, startIndex, ignoreCase = !filter.caseSensitive)
                if (idx == -1) break
                positions.add(idx until (idx + needle.length))
                startIndex = idx + needle.length
            }
        }
        return positions
    }
}

private class SearchDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "overgram_search.db", null, 1) {

    override fun onCreate(db: AndroidSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE saved_searches (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                filter_data TEXT NOT NULL,
                result_count INTEGER DEFAULT 0,
                created_at INTEGER NOT NULL,
                last_used INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_last_used ON saved_searches(last_used)")
    }

    override fun onUpgrade(db: AndroidSQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // no-op
    }
}
