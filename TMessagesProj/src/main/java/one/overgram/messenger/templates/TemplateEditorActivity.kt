package one.overgram.messenger.templates

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Activity for creating and editing message templates
 *
 * Features:
 * - Edit title and content
 * - Select category
 * - Insert variables with buttons
 * - Live preview
 * - Validation
 */
class TemplateEditorActivity : AppCompatActivity() {

    private lateinit var templateManager: TemplateManager
    private var editingTemplate: MessageTemplate? = null

    private lateinit var titleInput: TextInputEditText
    private lateinit var titleLayout: TextInputLayout
    private lateinit var contentInput: TextInputEditText
    private lateinit var contentLayout: TextInputLayout
    private lateinit var categorySpinner: Spinner
    private lateinit var variableChipGroup: ChipGroup
    private lateinit var previewText: TextView
    private lateinit var favoriteCheckbox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_template_editor)

        templateManager = TemplateManager.getInstance(this)

        setupActionBar()
        setupViews()
        loadTemplate()
    }

    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = if (intent.hasExtra("template_id")) "Edit Template" else "New Template"
        }
    }

    private fun setupViews() {
        titleInput = findViewById(R.id.template_title_input)
        titleLayout = findViewById(R.id.template_title_layout)
        contentInput = findViewById(R.id.template_content_input)
        contentLayout = findViewById(R.id.template_content_layout)
        categorySpinner = findViewById(R.id.template_category_spinner)
        variableChipGroup = findViewById(R.id.template_variable_chips)
        previewText = findViewById(R.id.template_preview)
        favoriteCheckbox = findViewById(R.id.template_favorite_checkbox)

        setupCategorySpinner()
        setupVariableButtons()
        setupContentListener()
    }

    private fun setupCategorySpinner() {
        val categories = TemplateCategory.DEFAULT_CATEGORIES.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    private fun setupVariableButtons() {
        val variables = listOf(
            MessageTemplate.VAR_NAME to "Contact Name",
            MessageTemplate.VAR_FIRSTNAME to "First Name",
            MessageTemplate.VAR_LASTNAME to "Last Name",
            MessageTemplate.VAR_DATE to "Date",
            MessageTemplate.VAR_TIME to "Time",
            MessageTemplate.VAR_DAY to "Day",
            MessageTemplate.VAR_DATETIME to "Date & Time"
        )

        variables.forEach { (variable, label) ->
            val chip = Chip(this).apply {
                text = label
                setOnClickListener {
                    insertVariable(variable)
                }
            }
            variableChipGroup.addView(chip)
        }

        // Add custom variable button
        val customChip = Chip(this).apply {
            text = "+ Custom"
            setOnClickListener {
                showCustomVariableDialog()
            }
        }
        variableChipGroup.addView(customChip)
    }

    private fun setupContentListener() {
        contentInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                updatePreview()
            }
        })
    }

    private fun loadTemplate() {
        val templateId = intent.getLongExtra("template_id", 0)
        if (templateId != 0L) {
            editingTemplate = templateManager.getTemplate(templateId)
            editingTemplate?.let { template ->
                titleInput.setText(template.title)
                contentInput.setText(template.content)

                // Set category
                val categories = TemplateCategory.DEFAULT_CATEGORIES.map { it.name }
                val categoryIndex = categories.indexOf(template.category)
                if (categoryIndex >= 0) {
                    categorySpinner.setSelection(categoryIndex)
                }

                favoriteCheckbox.isChecked = template.isFavorite
                updatePreview()
            }
        }
    }

    private fun insertVariable(variable: String) {
        val start = contentInput.selectionStart.coerceAtLeast(0)
        val end = contentInput.selectionEnd.coerceAtLeast(0)
        val text = contentInput.text ?: return

        val variableText = "{$variable}"
        text.replace(start, end, variableText)

        // Move cursor after inserted variable
        contentInput.setSelection(start + variableText.length)
    }

    private fun showCustomVariableDialog() {
        val input = EditText(this)
        input.hint = "Variable name (e.g., company, project)"

        android.app.AlertDialog.Builder(this)
            .setTitle("Custom Variable")
            .setMessage("Enter a name for your custom variable:")
            .setView(input)
            .setPositiveButton("Insert") { _, _ ->
                val varName = input.text.toString().trim()
                if (varName.isNotEmpty()) {
                    insertVariable(varName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updatePreview() {
        val content = contentInput.text?.toString() ?: ""
        if (content.isEmpty()) {
            previewText.text = "Preview will appear here..."
            return
        }

        // Create temporary template for preview
        val tempTemplate = MessageTemplate(
            title = titleInput.text?.toString() ?: "Untitled",
            content = content
        )

        // Process with sample data
        val result = templateManager.processTemplate(
            template = tempTemplate,
            contactName = "John Doe",
            customVariables = emptyMap()
        )

        previewText.text = result.text

        // Show unresolved variables warning
        if (result.unresolvedVariables.isNotEmpty()) {
            previewText.append("\n\n⚠️ Unresolved variables: ${result.unresolvedVariables.joinToString(", ")}")
        }
    }

    private fun validateAndSave(): Boolean {
        val title = titleInput.text?.toString()?.trim() ?: ""
        val content = contentInput.text?.toString()?.trim() ?: ""
        val category = categorySpinner.selectedItem as String
        val isFavorite = favoriteCheckbox.isChecked

        var isValid = true

        // Validate title
        if (title.isEmpty()) {
            titleLayout.error = "Title is required"
            isValid = false
        } else {
            titleLayout.error = null
        }

        // Validate content
        if (content.isEmpty()) {
            contentLayout.error = "Content is required"
            isValid = false
        } else if (content.length > 4000) {
            contentLayout.error = "Content is too long (max 4000 characters)"
            isValid = false
        } else {
            contentLayout.error = null
        }

        if (!isValid) return false

        // Create or update template
        val template = if (editingTemplate != null) {
            editingTemplate!!.copy(
                title = title,
                content = content,
                category = category,
                isFavorite = isFavorite,
                updatedAt = System.currentTimeMillis()
            )
        } else {
            MessageTemplate(
                title = title,
                content = content,
                category = category,
                isFavorite = isFavorite,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        }

        templateManager.saveTemplate(template)
        Toast.makeText(this, "Template saved", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.template_editor_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_save -> {
                validateAndSave()
                true
            }
            R.id.action_preview -> {
                showPreviewDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showPreviewDialog() {
        val content = contentInput.text?.toString() ?: ""
        val tempTemplate = MessageTemplate(
            title = titleInput.text?.toString() ?: "Untitled",
            content = content
        )

        val result = templateManager.processTemplate(
            template = tempTemplate,
            contactName = "John Doe"
        )

        android.app.AlertDialog.Builder(this)
            .setTitle("Preview")
            .setMessage(result.text)
            .setPositiveButton("OK", null)
            .show()
    }
}
