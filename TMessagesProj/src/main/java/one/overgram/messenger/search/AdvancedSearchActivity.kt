package one.overgram.messenger.search

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Advanced search activity with comprehensive filters
 *
 * Features:
 * - Text search with regex support
 * - Media type filters
 * - Date range picker
 * - File size filters
 * - Message type filters
 * - Save search queries
 * - Live search results
 */
class AdvancedSearchActivity : AppCompatActivity() {

    private lateinit var searchManager: AdvancedSearchManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchResultsAdapter
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var emptyView: TextView
    private lateinit var resultCountText: TextView

    // Filter UI components
    private lateinit var queryInput: TextInputEditText
    private lateinit var regexCheckbox: CheckBox
    private lateinit var caseSensitiveCheckbox: CheckBox
    private lateinit var mediaTypeSpinner: Spinner
    private lateinit var messageTypeSpinner: Spinner
    private lateinit var dateFromButton: Button
    private lateinit var dateToButton: Button
    private lateinit var filtersChipGroup: ChipGroup

    private var currentFilter = AdvancedSearchFilter()
    private var searchResults: List<SearchResult> = emptyList()
    private var dateFrom: Long? = null
    private var dateTo: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_search)

        searchManager = AdvancedSearchManager.getInstance(this)

        setupActionBar()
        setupViews()
        setupFilters()
    }

    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Advanced Search"
        }
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.search_results_recycler)
        progressBar = findViewById(R.id.search_progress)
        emptyView = findViewById(R.id.search_empty_view)
        resultCountText = findViewById(R.id.search_result_count)

        adapter = SearchResultsAdapter { result ->
            openMessage(result)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btn_search).setOnClickListener {
            performSearch()
        }

        findViewById<Button>(R.id.btn_clear_filters).setOnClickListener {
            clearFilters()
        }
    }

    private fun setupFilters() {
        queryInput = findViewById(R.id.search_query_input)
        regexCheckbox = findViewById(R.id.search_regex_checkbox)
        caseSensitiveCheckbox = findViewById(R.id.search_case_sensitive_checkbox)
        mediaTypeSpinner = findViewById(R.id.search_media_type_spinner)
        messageTypeSpinner = findViewById(R.id.search_message_type_spinner)
        dateFromButton = findViewById(R.id.search_date_from_button)
        dateToButton = findViewById(R.id.search_date_to_button)
        filtersChipGroup = findViewById(R.id.search_active_filters)

        setupMediaTypeSpinner()
        setupMessageTypeSpinner()
        setupDatePickers()
        setupAdvancedFilters()
    }

    private fun setupMediaTypeSpinner() {
        val mediaTypes = MediaType.values().map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mediaTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mediaTypeSpinner.adapter = adapter
    }

    private fun setupMessageTypeSpinner() {
        val messageTypes = MessageType.values().map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, messageTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        messageTypeSpinner.adapter = adapter
    }

    private fun setupDatePickers() {
        dateFromButton.setOnClickListener {
            showDatePicker { date ->
                dateFrom = date
                updateDateButton(dateFromButton, date)
                updateActiveFilters()
            }
        }

        dateToButton.setOnClickListener {
            showDatePicker { date ->
                dateTo = date
                updateDateButton(dateToButton, date)
                updateActiveFilters()
            }
        }
    }

    private fun setupAdvancedFilters() {
        findViewById<CheckBox>(R.id.filter_only_edited).setOnCheckedChangeListener { _, _ ->
            updateActiveFilters()
        }

        findViewById<CheckBox>(R.id.filter_only_forwarded).setOnCheckedChangeListener { _, _ ->
            updateActiveFilters()
        }

        findViewById<CheckBox>(R.id.filter_only_replies).setOnCheckedChangeListener { _, _ ->
            updateActiveFilters()
        }

        findViewById<CheckBox>(R.id.filter_has_links).setOnCheckedChangeListener { _, _ ->
            updateActiveFilters()
        }

        findViewById<CheckBox>(R.id.filter_has_hashtags).setOnCheckedChangeListener { _, _ ->
            updateActiveFilters()
        }

        findViewById<CheckBox>(R.id.filter_has_mentions).setOnCheckedChangeListener { _, _ ->
            updateActiveFilters()
        }
    }

    private fun showDatePicker(onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                onDateSelected(calendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateButton(button: Button, date: Long) {
        val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        button.text = format.format(Date(date))
    }

    private fun updateActiveFilters() {
        filtersChipGroup.removeAllViews()

        val filter = buildFilter()
        val count = filter.getActiveFilterCount()

        if (count > 0) {
            addFilterChip("$count filters active")

            if (dateFrom != null || dateTo != null) {
                addFilterChip("Date range", true) {
                    dateFrom = null
                    dateTo = null
                    dateFromButton.text = "From"
                    dateToButton.text = "To"
                    updateActiveFilters()
                }
            }

            if (filter.mediaType != MediaType.ALL) {
                addFilterChip("Media: ${filter.mediaType.name}", true) {
                    mediaTypeSpinner.setSelection(0)
                    updateActiveFilters()
                }
            }

            if (filter.onlyEdited) {
                addFilterChip("Edited only", true) {
                    findViewById<CheckBox>(R.id.filter_only_edited).isChecked = false
                }
            }
        }
    }

    private fun addFilterChip(text: String, closeable: Boolean = false, onClose: (() -> Unit)? = null) {
        val chip = Chip(this).apply {
            this.text = text
            isCloseIconVisible = closeable
            setOnCloseIconClickListener {
                onClose?.invoke()
            }
        }
        filtersChipGroup.addView(chip)
    }

    private fun buildFilter(): AdvancedSearchFilter {
        return AdvancedSearchFilter(
            query = queryInput.text?.toString() ?: "",
            useRegex = regexCheckbox.isChecked,
            caseSensitive = caseSensitiveCheckbox.isChecked,
            mediaType = MediaType.values()[mediaTypeSpinner.selectedItemPosition],
            messageType = MessageType.values()[messageTypeSpinner.selectedItemPosition],
            dateFrom = dateFrom,
            dateTo = dateTo,
            onlyEdited = findViewById<CheckBox>(R.id.filter_only_edited).isChecked,
            onlyForwarded = findViewById<CheckBox>(R.id.filter_only_forwarded).isChecked,
            onlyReplies = findViewById<CheckBox>(R.id.filter_only_replies).isChecked,
            hasLinks = if (findViewById<CheckBox>(R.id.filter_has_links).isChecked) true else null,
            hasHashtags = if (findViewById<CheckBox>(R.id.filter_has_hashtags).isChecked) true else null,
            hasMentions = if (findViewById<CheckBox>(R.id.filter_has_mentions).isChecked) true else null
        )
    }

    private fun performSearch() {
        currentFilter = buildFilter()

        if (!currentFilter.hasActiveFilters()) {
            Toast.makeText(this, "Please enter search criteria", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentFilter.useRegex && !currentFilter.isValidRegex()) {
            Toast.makeText(this, "Invalid regular expression", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            showLoading(true)

            try {
                val results = searchManager.search(
                    filter = currentFilter,
                    onProgress = { count ->
                        runOnUiThread {
                            progressBar.progress = count
                        }
                    }
                )

                searchResults = results
                adapter.submitList(results)
                updateResultsView()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@AdvancedSearchActivity,
                    "Search failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun updateResultsView() {
        if (searchResults.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            resultCountText.text = "No results found"
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            resultCountText.text = "${searchResults.size} results found"
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        findViewById<Button>(R.id.btn_search).isEnabled = !show
    }

    private fun clearFilters() {
        queryInput.text?.clear()
        regexCheckbox.isChecked = false
        caseSensitiveCheckbox.isChecked = false
        mediaTypeSpinner.setSelection(0)
        messageTypeSpinner.setSelection(0)
        dateFrom = null
        dateTo = null
        dateFromButton.text = "From"
        dateToButton.text = "To"
        findViewById<CheckBox>(R.id.filter_only_edited).isChecked = false
        findViewById<CheckBox>(R.id.filter_only_forwarded).isChecked = false
        findViewById<CheckBox>(R.id.filter_only_replies).isChecked = false
        findViewById<CheckBox>(R.id.filter_has_links).isChecked = false
        findViewById<CheckBox>(R.id.filter_has_hashtags).isChecked = false
        findViewById<CheckBox>(R.id.filter_has_mentions).isChecked = false

        updateActiveFilters()
        searchResults = emptyList()
        adapter.submitList(emptyList())
        updateResultsView()
    }

    private fun openMessage(result: SearchResult) {
        // TODO: Implement navigation to message
        Toast.makeText(this, "Opening message ${result.messageId}", Toast.LENGTH_SHORT).show()
    }

    private fun saveCurrentSearch() {
        val input = EditText(this)
        input.hint = "Search name"

        android.app.AlertDialog.Builder(this)
            .setTitle("Save Search")
            .setMessage("Enter a name for this search:")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    val savedSearch = SavedSearch(
                        name = name,
                        filter = currentFilter,
                        resultCount = searchResults.size
                    )
                    searchManager.saveSearch(savedSearch)
                    Toast.makeText(this, "Search saved", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.advanced_search_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_save_search -> {
                saveCurrentSearch()
                true
            }
            R.id.action_saved_searches -> {
                showSavedSearches()
                true
            }
            R.id.action_export_results -> {
                exportResults()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSavedSearches() {
        // TODO: Implement saved searches dialog
        Toast.makeText(this, "Saved searches coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun exportResults() {
        // TODO: Implement export
        Toast.makeText(this, "Export coming soon", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        searchManager.cleanup()
    }
}
