package one.overgram.messenger.templates

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.telegram.messenger.R

/**
 * Dialog for entering custom variable values for templates
 *
 * Features:
 * - Auto-detect required variables
 * - Pre-fill built-in variables (name, date, etc.)
 * - Live preview of processed template
 * - Validation
 */
class VariableInputDialog : AppCompatActivity() {

    private lateinit var templateManager: TemplateManager
    private lateinit var template: MessageTemplate
    private lateinit var variablesContainer: LinearLayout
    private lateinit var previewText: TextView
    private lateinit var submitButton: Button

    private val variableInputs = mutableMapOf<String, TextInputEditText>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_variable_input)

        templateManager = TemplateManager.getInstance(this)

        template = intent.getParcelableExtra("template") ?: run {
            finish()
            return
        }

        setupViews()
        createVariableInputs()
        updatePreview()
    }

    private fun setupViews() {
        variablesContainer = findViewById(R.id.variables_container)
        previewText = findViewById(R.id.variable_preview)
        submitButton = findViewById(R.id.btn_submit)

        submitButton.setOnClickListener {
            processAndReturn()
        }

        findViewById<TextView>(R.id.variable_dialog_title).text = template.title
        findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            finish()
        }
    }

    private fun createVariableInputs() {
        val variables = template.getVariables()
        val builtInVars = setOf(
            MessageTemplate.VAR_NAME,
            MessageTemplate.VAR_FIRSTNAME,
            MessageTemplate.VAR_LASTNAME,
            MessageTemplate.VAR_DATE,
            MessageTemplate.VAR_TIME,
            MessageTemplate.VAR_DAY,
            MessageTemplate.VAR_DATETIME
        )

        variables.forEach { variable ->
            // Skip built-in variables that don't need input
            if (variable.lowercase() in builtInVars &&
                variable.lowercase() != MessageTemplate.VAR_NAME &&
                variable.lowercase() != MessageTemplate.VAR_FIRSTNAME &&
                variable.lowercase() != MessageTemplate.VAR_LASTNAME) {
                return@forEach
            }

            val inputLayout = TextInputLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 16
                }
                hint = formatVariableName(variable)
            }

            val input = TextInputEditText(this).apply {
                addTextChangedListener(object : android.text.TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: android.text.Editable?) {
                        updatePreview()
                    }
                })
            }

            inputLayout.addView(input)
            variablesContainer.addView(inputLayout)
            variableInputs[variable] = input
        }

        if (variableInputs.isEmpty()) {
            // No custom variables needed, just show preview
            variablesContainer.visibility = View.GONE
            findViewById<TextView>(R.id.variable_input_message).text =
                "This template has no custom variables. Click submit to use it."
        }
    }

    private fun formatVariableName(variable: String): String {
        return when (variable.lowercase()) {
            MessageTemplate.VAR_NAME -> "Contact Name"
            MessageTemplate.VAR_FIRSTNAME -> "First Name"
            MessageTemplate.VAR_LASTNAME -> "Last Name"
            else -> variable.replaceFirstChar { it.uppercase() }
        }
    }

    private fun updatePreview() {
        val customVars = variableInputs.mapValues { (_, input) ->
            input.text?.toString() ?: ""
        }

        val result = templateManager.processTemplate(
            template = template,
            contactName = customVars[MessageTemplate.VAR_NAME],
            customVariables = customVars
        )

        previewText.text = result.text
    }

    private fun processAndReturn() {
        val customVars = variableInputs.mapValues { (_, input) ->
            input.text?.toString() ?: ""
        }

        // Validate required variables
        val emptyVars = customVars.filter { it.value.isEmpty() }
        if (emptyVars.isNotEmpty()) {
            Toast.makeText(
                this,
                "Please fill in all variables",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val result = templateManager.processTemplate(
            template = template,
            contactName = customVars[MessageTemplate.VAR_NAME],
            customVariables = customVars
        )

        // Increment use count
        templateManager.incrementUseCount(template.id)

        // Return processed text
        val intent = android.content.Intent().apply {
            putExtra("processed_text", result.text)
        }
        setResult(RESULT_OK, intent)
        finish()
    }
}

/**
 * Simple dialog version for quick variable input
 */
object SimpleVariableInputDialog {

    fun show(
        context: Context,
        template: MessageTemplate,
        onResult: (String) -> Unit
    ) {
        val variables = template.getVariables()
        val customVars = mutableMapOf<String, String>()

        if (variables.isEmpty()) {
            onResult(template.content)
            return
        }

        showVariableDialog(context, template, variables.toMutableList(), customVars, onResult)
    }

    private fun showVariableDialog(
        context: Context,
        template: MessageTemplate,
        remainingVars: MutableList<String>,
        customVars: MutableMap<String, String>,
        onResult: (String) -> Unit
    ) {
        if (remainingVars.isEmpty()) {
            // All variables collected, process template
            val templateManager = TemplateManager.getInstance(context)
            val result = templateManager.processTemplate(
                template = template,
                contactName = customVars[MessageTemplate.VAR_NAME],
                customVariables = customVars
            )
            templateManager.incrementUseCount(template.id)
            onResult(result.text)
            return
        }

        val variable = remainingVars.removeAt(0)

        // Skip built-in auto-filled variables
        val autoVars = setOf(
            MessageTemplate.VAR_DATE,
            MessageTemplate.VAR_TIME,
            MessageTemplate.VAR_DAY,
            MessageTemplate.VAR_DATETIME
        )
        if (variable.lowercase() in autoVars) {
            showVariableDialog(context, template, remainingVars, customVars, onResult)
            return
        }

        val input = EditText(context)
        input.hint = formatVariableName(variable)

        AlertDialog.Builder(context)
            .setTitle("Enter ${formatVariableName(variable)}")
            .setMessage("This template requires: {$variable}")
            .setView(input)
            .setPositiveButton("Next") { _, _ ->
                customVars[variable] = input.text.toString()
                showVariableDialog(context, template, remainingVars, customVars, onResult)
            }
            .setNegativeButton("Cancel", null)
            .setCancelable(false)
            .show()
    }

    private fun formatVariableName(variable: String): String {
        return when (variable.lowercase()) {
            MessageTemplate.VAR_NAME -> "Contact Name"
            MessageTemplate.VAR_FIRSTNAME -> "First Name"
            MessageTemplate.VAR_LASTNAME -> "Last Name"
            else -> variable.replaceFirstChar { it.uppercase() }
        }
    }
}
