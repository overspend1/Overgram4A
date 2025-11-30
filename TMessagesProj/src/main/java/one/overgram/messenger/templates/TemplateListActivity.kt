package one.overgram.messenger.templates

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout

/**
 * Activity for browsing and managing message templates
 *
 * Features:
 * - Browse all templates or by category
 * - Favorite templates
 * - Search templates
 * - Create/edit/delete templates
 * - Export/import templates
 */
class TemplateListActivity : AppCompatActivity() {

    private lateinit var templateManager: TemplateManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TemplateAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var searchView: SearchView
    private lateinit var emptyView: TextView

    private var currentCategory: String = "All"
    private var templates: List<MessageTemplate> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_template_list)

        templateManager = TemplateManager.getInstance(this)

        setupActionBar()
        setupViews()
        setupTabs()
        loadTemplates()
    }

    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Message Templates"
        }
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.template_recycler_view)
        tabLayout = findViewById(R.id.template_tabs)
        emptyView = findViewById(R.id.empty_view)

        adapter = TemplateAdapter(
            onTemplateClick = { template -> useTemplate(template) },
            onTemplateLongClick = { template -> showTemplateOptions(template) },
            onFavoriteClick = { template -> toggleFavorite(template) }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fab_add_template).setOnClickListener {
            createNewTemplate()
        }
    }

    private fun setupTabs() {
        // Add "All" tab
        tabLayout.addTab(tabLayout.newTab().setText("All"))

        // Add "Favorites" tab
        tabLayout.addTab(tabLayout.newTab().setText("⭐ Favorites"))

        // Add category tabs
        val categories = templateManager.getCategories()
        categories.forEach { category ->
            tabLayout.addTab(tabLayout.newTab().setText(category))
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> currentCategory = "All"
                    1 -> currentCategory = "Favorites"
                    else -> currentCategory = categories[tab.position - 2]
                }
                loadTemplates()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun loadTemplates() {
        templates = when (currentCategory) {
            "All" -> templateManager.getAllTemplates()
            "Favorites" -> templateManager.getFavoriteTemplates()
            else -> templateManager.getTemplatesByCategory(currentCategory)
        }

        adapter.submitList(templates)
        updateEmptyView()
    }

    private fun updateEmptyView() {
        if (templates.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            emptyView.text = when (currentCategory) {
                "Favorites" -> "No favorite templates yet.\nTap ⭐ to mark templates as favorites."
                "All" -> "No templates yet.\nTap + to create your first template."
                else -> "No templates in this category."
            }
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun useTemplate(template: MessageTemplate) {
        templateManager.incrementUseCount(template.id)

        if (template.hasVariables()) {
            // Show variable input dialog
            showVariableInputDialog(template)
        } else {
            // Return template directly
            returnTemplate(template.content)
        }
    }

    private fun showVariableInputDialog(template: MessageTemplate) {
        val intent = Intent(this, VariableInputDialog::class.java).apply {
            putExtra("template", template)
        }
        startActivityForResult(intent, REQUEST_VARIABLE_INPUT)
    }

    private fun showTemplateOptions(template: MessageTemplate) {
        val options = arrayOf(
            "Edit",
            "Duplicate",
            if (template.isFavorite) "Remove from favorites" else "Add to favorites",
            "Share",
            "Delete"
        )

        AlertDialog.Builder(this)
            .setTitle(template.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editTemplate(template)
                    1 -> duplicateTemplate(template)
                    2 -> toggleFavorite(template)
                    3 -> shareTemplate(template)
                    4 -> deleteTemplate(template)
                }
            }
            .show()
    }

    private fun editTemplate(template: MessageTemplate) {
        val intent = Intent(this, TemplateEditorActivity::class.java).apply {
            putExtra("template_id", template.id)
        }
        startActivityForResult(intent, REQUEST_EDIT_TEMPLATE)
    }

    private fun duplicateTemplate(template: MessageTemplate) {
        val duplicate = template.copy(
            id = 0,
            title = "${template.title} (Copy)",
            createdAt = System.currentTimeMillis()
        )
        templateManager.saveTemplate(duplicate)
        loadTemplates()
        Toast.makeText(this, "Template duplicated", Toast.LENGTH_SHORT).show()
    }

    private fun toggleFavorite(template: MessageTemplate) {
        val isFavorite = templateManager.toggleFavorite(template.id)
        loadTemplates()
        Toast.makeText(
            this,
            if (isFavorite) "Added to favorites" else "Removed from favorites",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun shareTemplate(template: MessageTemplate) {
        val shareText = buildString {
            append("📝 ${template.title}\n\n")
            append(template.content)
            if (template.hasVariables()) {
                append("\n\nVariables: ${template.getVariables().joinToString(", ") { "{$it}" }}")
            }
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Share template"))
    }

    private fun deleteTemplate(template: MessageTemplate) {
        AlertDialog.Builder(this)
            .setTitle("Delete template?")
            .setMessage("Are you sure you want to delete \"${template.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                templateManager.deleteTemplate(template.id)
                loadTemplates()
                Toast.makeText(this, "Template deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createNewTemplate() {
        startActivityForResult(
            Intent(this, TemplateEditorActivity::class.java),
            REQUEST_CREATE_TEMPLATE
        )
    }

    private fun searchTemplates(query: String) {
        templates = if (query.isEmpty()) {
            when (currentCategory) {
                "All" -> templateManager.getAllTemplates()
                "Favorites" -> templateManager.getFavoriteTemplates()
                else -> templateManager.getTemplatesByCategory(currentCategory)
            }
        } else {
            templateManager.searchTemplates(query)
        }
        adapter.submitList(templates)
        updateEmptyView()
    }

    private fun exportTemplates() {
        val json = templateManager.exportTemplates()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_TEXT, json)
            putExtra(Intent.EXTRA_SUBJECT, "Overgram Templates Export")
        }
        startActivity(Intent.createChooser(intent, "Export templates"))
    }

    private fun returnTemplate(text: String) {
        val intent = Intent().apply {
            putExtra("template_text", text)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.template_list_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchTemplates(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchTemplates(newText)
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_export -> {
                exportTemplates()
                true
            }
            R.id.action_import -> {
                // TODO: Implement import
                Toast.makeText(this, "Import coming soon", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CREATE_TEMPLATE, REQUEST_EDIT_TEMPLATE -> loadTemplates()
                REQUEST_VARIABLE_INPUT -> {
                    data?.getStringExtra("processed_text")?.let { text ->
                        returnTemplate(text)
                    }
                }
            }
        }
    }

    companion object {
        const val REQUEST_CREATE_TEMPLATE = 1
        const val REQUEST_EDIT_TEMPLATE = 2
        const val REQUEST_VARIABLE_INPUT = 3
    }
}

/**
 * RecyclerView adapter for template list
 */
class TemplateAdapter(
    private val onTemplateClick: (MessageTemplate) -> Unit,
    private val onTemplateLongClick: (MessageTemplate) -> Unit,
    private val onFavoriteClick: (MessageTemplate) -> Unit
) : RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder>() {

    private var templates: List<MessageTemplate> = emptyList()

    fun submitList(newTemplates: List<MessageTemplate>) {
        templates = newTemplates
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_template, parent, false)
        return TemplateViewHolder(view)
    }

    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        holder.bind(templates[position])
    }

    override fun getItemCount() = templates.size

    inner class TemplateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.template_title)
        private val previewText: TextView = itemView.findViewById(R.id.template_preview)
        private val categoryBadge: TextView = itemView.findViewById(R.id.template_category)
        private val favoriteButton: ImageButton = itemView.findViewById(R.id.template_favorite)
        private val useCountText: TextView = itemView.findViewById(R.id.template_use_count)
        private val variableIndicator: ImageView = itemView.findViewById(R.id.template_has_variables)

        fun bind(template: MessageTemplate) {
            titleText.text = template.title
            previewText.text = template.getPreview()
            categoryBadge.text = template.category
            useCountText.text = "Used ${template.useCount} times"

            favoriteButton.setImageResource(
                if (template.isFavorite) R.drawable.ic_star_filled else R.drawable.ic_star_outline
            )

            variableIndicator.visibility = if (template.hasVariables()) View.VISIBLE else View.GONE

            itemView.setOnClickListener { onTemplateClick(template) }
            itemView.setOnLongClickListener {
                onTemplateLongClick(template)
                true
            }
            favoriteButton.setOnClickListener { onFavoriteClick(template) }
        }
    }
}
