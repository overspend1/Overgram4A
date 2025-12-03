package one.overgram.messenger.search

import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.telegram.messenger.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView adapter for displaying search results
 *
 * Features:
 * - Highlighted search matches
 * - Message preview
 * - Date and sender info
 * - Relevance score indicator
 */
class SearchResultsAdapter(
    private val onResultClick: (SearchResult) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.SearchResultViewHolder>() {

    private var results: List<SearchResult> = emptyList()

    fun submitList(newResults: List<SearchResult>) {
        results = newResults
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return SearchResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(results[position])
    }

    override fun getItemCount() = results.size

    inner class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.result_message_text)
        private val chatName: TextView = itemView.findViewById(R.id.result_chat_name)
        private val dateText: TextView = itemView.findViewById(R.id.result_date)
        private val scoreIndicator: View = itemView.findViewById(R.id.result_score_indicator)

        fun bind(result: SearchResult) {
            // Display highlighted text
            messageText.text = getHighlightedText(result)

            // Display chat/sender info
            chatName.text = "Chat ${result.chatId}" // TODO: Get actual chat name

            // Display date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            dateText.text = dateFormat.format(Date(result.date.toLong() * 1000))

            // Show relevance score as color intensity
            val scoreAlpha = (result.score * 255).toInt().coerceIn(50, 255)
            scoreIndicator.setBackgroundColor(
                android.graphics.Color.argb(scoreAlpha, 33, 150, 243)
            )

            itemView.setOnClickListener {
                onResultClick(result)
            }
        }

        private fun getHighlightedText(result: SearchResult): Spanned {
            if (result.matchPositions.isEmpty()) {
                return Html.fromHtml(result.text, Html.FROM_HTML_MODE_LEGACY)
            }

            val highlighted = result.getHighlightedText()
            return Html.fromHtml(highlighted, Html.FROM_HTML_MODE_LEGACY)
        }
    }
}
