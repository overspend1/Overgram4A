package one.overgram.messenger.templates

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.telegram.messenger.R

/**
 * Quick selector for message templates
 * Shows as bottom sheet dialog from message input
 *
 * Features:
 * - Quick access to favorite templates
 * - Recent templates
 * - Search templates
 * - Category filter
 */
class TemplateQuickSelector(
    private val context: Context,
    private val onTemplateSelected: (MessageTemplate) -> Unit
) {

    private val templateManager = TemplateManager.getInstance(context)
    private lateinit var dialog: BottomSheetDialog
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var adapter: QuickTemplateAdapter

    private var templates: List<MessageTemplate> = emptyList()
    private var currentFilter = "Favorites"

    fun show() {
        dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_template_quick_selector, null)
        dialog.setContentView(view)

        setupViews(view)
        loadTemplates()

        dialog.show()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.quick_template_recycler)
        searchInput = view.findViewById(R.id.quick_template_search)
        categorySpinner = view.findViewById(R.id.quick_template_category)

        adapter = QuickTemplateAdapter { template ->
            dialog.dismiss()
            onTemplateSelected(template)
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        setupCategoryFilter(view)
        setupSearch()
        setupManageButton(view)
    }

    private fun setupCategoryFilter(view: View) {
        val filters = listOf("Favorites", "Recent", "All") + templateManager.getCategories()
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, filters)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentFilter = filters[position]
                loadTemplates()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSearch() {
        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                filterTemplates(s?.toString() ?: "")
            }
        })
    }

    private fun setupManageButton(view: View) {
        view.findViewById<Button>(R.id.btn_manage_templates).setOnClickListener {
            dialog.dismiss()
            val intent = android.content.Intent(context, TemplateListActivity::class.java)
            context.startActivity(intent)
        }
    }

    private fun loadTemplates() {
        templates = when (currentFilter) {
            "Favorites" -> templateManager.getFavoriteTemplates()
            "Recent" -> templateManager.getAllTemplates().sortedByDescending { it.updatedAt }.take(10)
            "All" -> templateManager.getAllTemplates()
            else -> templateManager.getTemplatesByCategory(currentFilter)
        }

        adapter.submitList(templates)
    }

    private fun filterTemplates(query: String) {
        val filtered = if (query.isEmpty()) {
            templates
        } else {
            templates.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true)
            }
        }
        adapter.submitList(filtered)
    }
}

/**
 * Compact adapter for quick template selection
 */
class QuickTemplateAdapter(
    private val onTemplateClick: (MessageTemplate) -> Unit
) : RecyclerView.Adapter<QuickTemplateAdapter.QuickTemplateViewHolder>() {

    private var templates: List<MessageTemplate> = emptyList()

    fun submitList(newTemplates: List<MessageTemplate>) {
        templates = newTemplates
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuickTemplateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quick_template, parent, false)
        return QuickTemplateViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuickTemplateViewHolder, position: Int) {
        holder.bind(templates[position])
    }

    override fun getItemCount() = templates.size

    inner class QuickTemplateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.quick_template_title)
        private val previewText: TextView = itemView.findViewById(R.id.quick_template_preview)
        private val categoryBadge: TextView = itemView.findViewById(R.id.quick_template_category)
        private val favoriteIcon: ImageView = itemView.findViewById(R.id.quick_template_favorite_icon)

        fun bind(template: MessageTemplate) {
            titleText.text = template.title
            previewText.text = template.getPreview()
            categoryBadge.text = template.category
            favoriteIcon.visibility = if (template.isFavorite) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                onTemplateClick(template)
            }
        }
    }
}
