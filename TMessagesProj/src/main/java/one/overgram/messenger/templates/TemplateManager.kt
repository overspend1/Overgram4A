package one.overgram.messenger.templates

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages message templates storage and retrieval
 */
class TemplateManager private constructor(context: Context) {

    private val dbHelper = TemplateDatabaseHelper(context)

    companion object {
        @Volatile
        private var instance: TemplateManager? = null

        fun getInstance(context: Context): TemplateManager {
            return instance ?: synchronized(this) {
                instance ?: TemplateManager(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Get all templates
     */
    fun getAllTemplates(): List<MessageTemplate> {
        val templates = mutableListOf<MessageTemplate>()
        val db = dbHelper.readableDatabase

        db.query(
            TemplateDatabaseHelper.TABLE_TEMPLATES,
            null,
            null,
            null,
            null,
            null,
            "${TemplateDatabaseHelper.COLUMN_IS_FAVORITE} DESC, ${TemplateDatabaseHelper.COLUMN_USE_COUNT} DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                templates.add(cursorToTemplate(cursor))
            }
        }

        return templates
    }

    /**
     * Get templates by category
     */
    fun getTemplatesByCategory(category: String): List<MessageTemplate> {
        val templates = mutableListOf<MessageTemplate>()
        val db = dbHelper.readableDatabase

        db.query(
            TemplateDatabaseHelper.TABLE_TEMPLATES,
            null,
            "${TemplateDatabaseHelper.COLUMN_CATEGORY} = ?",
            arrayOf(category),
            null,
            null,
            "${TemplateDatabaseHelper.COLUMN_USE_COUNT} DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                templates.add(cursorToTemplate(cursor))
            }
        }

        return templates
    }

    /**
     * Get favorite templates
     */
    fun getFavoriteTemplates(): List<MessageTemplate> {
        val templates = mutableListOf<MessageTemplate>()
        val db = dbHelper.readableDatabase

        db.query(
            TemplateDatabaseHelper.TABLE_TEMPLATES,
            null,
            "${TemplateDatabaseHelper.COLUMN_IS_FAVORITE} = 1",
            null,
            null,
            null,
            "${TemplateDatabaseHelper.COLUMN_USE_COUNT} DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                templates.add(cursorToTemplate(cursor))
            }
        }

        return templates
    }

    /**
     * Get template by ID
     */
    fun getTemplate(id: Long): MessageTemplate? {
        val db = dbHelper.readableDatabase

        db.query(
            TemplateDatabaseHelper.TABLE_TEMPLATES,
            null,
            "${TemplateDatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursorToTemplate(cursor)
            }
        }

        return null
    }

    /**
     * Save template
     */
    fun saveTemplate(template: MessageTemplate): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(TemplateDatabaseHelper.COLUMN_TITLE, template.title)
            put(TemplateDatabaseHelper.COLUMN_CONTENT, template.content)
            put(TemplateDatabaseHelper.COLUMN_CATEGORY, template.category)
            put(TemplateDatabaseHelper.COLUMN_USE_COUNT, template.useCount)
            put(TemplateDatabaseHelper.COLUMN_IS_FAVORITE, if (template.isFavorite) 1 else 0)
            put(TemplateDatabaseHelper.COLUMN_CREATED_AT, template.createdAt)
            put(TemplateDatabaseHelper.COLUMN_UPDATED_AT, System.currentTimeMillis())
        }

        return if (template.id == 0L) {
            db.insert(TemplateDatabaseHelper.TABLE_TEMPLATES, null, values)
        } else {
            db.update(
                TemplateDatabaseHelper.TABLE_TEMPLATES,
                values,
                "${TemplateDatabaseHelper.COLUMN_ID} = ?",
                arrayOf(template.id.toString())
            )
            template.id
        }
    }

    /**
     * Delete template
     */
    fun deleteTemplate(id: Long): Boolean {
        val db = dbHelper.writableDatabase
        val deleted = db.delete(
            TemplateDatabaseHelper.TABLE_TEMPLATES,
            "${TemplateDatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
        return deleted > 0
    }

    /**
     * Search templates
     */
    fun searchTemplates(query: String): List<MessageTemplate> {
        val templates = mutableListOf<MessageTemplate>()
        val db = dbHelper.readableDatabase

        db.query(
            TemplateDatabaseHelper.TABLE_TEMPLATES,
            null,
            "${TemplateDatabaseHelper.COLUMN_TITLE} LIKE ? OR ${TemplateDatabaseHelper.COLUMN_CONTENT} LIKE ?",
            arrayOf("%$query%", "%$query%"),
            null,
            null,
            "${TemplateDatabaseHelper.COLUMN_USE_COUNT} DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                templates.add(cursorToTemplate(cursor))
            }
        }

        return templates
    }

    /**
     * Get all categories
     */
    fun getCategories(): List<String> {
        val categories = mutableSetOf<String>()
        val db = dbHelper.readableDatabase

        db.query(
            TemplateDatabaseHelper.TABLE_TEMPLATES,
            arrayOf(TemplateDatabaseHelper.COLUMN_CATEGORY),
            null,
            null,
            TemplateDatabaseHelper.COLUMN_CATEGORY,
            null,
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                categories.add(cursor.getString(0))
            }
        }

        return categories.toList()
    }

    /**
     * Increment template use count
     */
    fun incrementUseCount(id: Long) {
        val template = getTemplate(id) ?: return
        saveTemplate(template.incrementUseCount())
    }

    /**
     * Toggle favorite status
     */
    fun toggleFavorite(id: Long): Boolean {
        val template = getTemplate(id) ?: return false
        saveTemplate(template.toggleFavorite())
        return template.isFavorite
    }

    /**
     * Process template with variable replacement
     */
    fun processTemplate(
        template: MessageTemplate,
        contactName: String? = null,
        customVariables: Map<String, String> = emptyMap()
    ): TemplateResult {
        var text = template.content
        val unresolvedVars = mutableListOf<String>()

        // Built-in variables
        val now = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val datetimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        val builtInVars = mapOf(
            MessageTemplate.VAR_NAME to (contactName ?: "{name}"),
            MessageTemplate.VAR_DATE to dateFormat.format(Date(now)),
            MessageTemplate.VAR_TIME to timeFormat.format(Date(now)),
            MessageTemplate.VAR_DAY to dayFormat.format(Date(now)),
            MessageTemplate.VAR_DATETIME to datetimeFormat.format(Date(now)),
            MessageTemplate.VAR_FIRSTNAME to (contactName?.split(" ")?.firstOrNull() ?: "{firstname}"),
            MessageTemplate.VAR_LASTNAME to (contactName?.split(" ")?.lastOrNull() ?: "{lastname}")
        )

        // Replace all variables
        template.getVariables().forEach { variable ->
            val value = customVariables[variable]
                ?: builtInVars[variable.lowercase()]
                ?: run {
                    unresolvedVars.add(variable)
                    null
                }

            if (value != null && !value.startsWith("{")) {
                text = text.replace("{$variable}", value)
            }
        }

        return TemplateResult(text, unresolvedVars)
    }

    /**
     * Export templates to JSON
     */
    fun exportTemplates(): String {
        val templates = getAllTemplates()
        return buildString {
            append("[\n")
            templates.forEachIndexed { index, template ->
                append("  {\n")
                append("    \"title\": \"${template.title.escapeJson()}\",\n")
                append("    \"content\": \"${template.content.escapeJson()}\",\n")
                append("    \"category\": \"${template.category}\",\n")
                append("    \"isFavorite\": ${template.isFavorite}\n")
                append("  }")
                if (index < templates.size - 1) append(",")
                append("\n")
            }
            append("]")
        }
    }

    /**
     * Get template count
     */
    fun getTemplateCount(): Int {
        val db = dbHelper.readableDatabase
        db.rawQuery("SELECT COUNT(*) FROM ${TemplateDatabaseHelper.TABLE_TEMPLATES}", null).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getInt(0)
            }
        }
        return 0
    }

    private fun cursorToTemplate(cursor: android.database.Cursor): MessageTemplate {
        return MessageTemplate(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(TemplateDatabaseHelper.COLUMN_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(TemplateDatabaseHelper.COLUMN_TITLE)),
            content = cursor.getString(cursor.getColumnIndexOrThrow(TemplateDatabaseHelper.COLUMN_CONTENT)),
            category = cursor.getString(cursor.getColumnIndexOrThrow(TemplateDatabaseHelper.COLUMN_CATEGORY)),
            useCount = cursor.getInt(cursor.getColumnIndexOrThrow(TemplateDatabaseHelper.COLUMN_USE_COUNT)),
            isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(TemplateDatabaseHelper.COLUMN_IS_FAVORITE)) == 1,
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(TemplateDatabaseHelper.COLUMN_CREATED_AT)),
            updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow(TemplateDatabaseHelper.COLUMN_UPDATED_AT))
        )
    }

    private fun String.escapeJson(): String {
        return this.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}

/**
 * SQLite database helper for templates
 */
private class TemplateDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "overgram_templates.db"
        const val DATABASE_VERSION = 1

        const val TABLE_TEMPLATES = "templates"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_USE_COUNT = "use_count"
        const val COLUMN_IS_FAVORITE = "is_favorite"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_UPDATED_AT = "updated_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_TEMPLATES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_CONTENT TEXT NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL DEFAULT 'General',
                $COLUMN_USE_COUNT INTEGER NOT NULL DEFAULT 0,
                $COLUMN_IS_FAVORITE INTEGER NOT NULL DEFAULT 0,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_UPDATED_AT INTEGER NOT NULL
            )
        """.trimIndent()

        db.execSQL(createTable)

        // Create indexes
        db.execSQL("CREATE INDEX idx_category ON $TABLE_TEMPLATES($COLUMN_CATEGORY)")
        db.execSQL("CREATE INDEX idx_favorite ON $TABLE_TEMPLATES($COLUMN_IS_FAVORITE)")
        db.execSQL("CREATE INDEX idx_use_count ON $TABLE_TEMPLATES($COLUMN_USE_COUNT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Future migrations go here
    }
}
