package com.marknguyen.themedallists

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class MedallistAdapter(
    private var items: List<ListItem>,
    private var favourites: Set<String>,
    private val onItemClick: (Medallist) -> Unit,
    private val onItemLongClick: (Medallist) -> Unit,
    private val onFavouriteClick: (Medallist) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedCountry: String? = null

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerText: TextView = view.findViewById(R.id.tv_header)
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val medalIcon: ImageView = view.findViewById(R.id.iv_medal)
        val nameText: TextView = view.findViewById(R.id.tv_name)
        val subtitleText: TextView = view.findViewById(R.id.tv_subtitle)
        val minorText: TextView = view.findViewById(R.id.tv_minor)
        val bookmarkBtn: ImageView = view.findViewById(R.id.btn_bookmark)
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is ListItem.Header -> VIEW_TYPE_HEADER
        is ListItem.Item -> VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(
                inflater.inflate(R.layout.item_header, parent, false)
            )
            else -> ItemViewHolder(
                inflater.inflate(R.layout.item_medallist, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.Header -> (holder as HeaderViewHolder).headerText.text = item.letter
            is ListItem.Item -> bindItem(holder as ItemViewHolder, item.medallist)
        }
    }

    private fun bindItem(holder: ItemViewHolder, m: Medallist) {
        val ctx = holder.itemView.context

        holder.nameText.text = m.country
        holder.subtitleText.text = "${m.iocCode} • ${m.timesCompeted} appearances"
        holder.minorText.text = "Gold: ${m.gold}  •  Silver: ${m.silver}  •  Bronze: ${m.bronze}"

        val medalColorRes: Int
        val bgColorRes: Int
        when {
            m.gold > 0   -> { medalColorRes = R.color.medal_gold;   bgColorRes = R.color.bg_gold }
            m.silver > 0 -> { medalColorRes = R.color.medal_silver; bgColorRes = R.color.bg_silver }
            m.bronze > 0 -> { medalColorRes = R.color.medal_bronze; bgColorRes = R.color.bg_bronze }
            else         -> { medalColorRes = R.color.text_minor;   bgColorRes = R.color.bg_no_medal }
        }

        holder.medalIcon.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(ctx, medalColorRes)
        )

        val isSelected = m.country == selectedCountry
        holder.itemView.setBackgroundColor(
            ContextCompat.getColor(ctx, if (isSelected) R.color.bg_selected else bgColorRes)
        )

        val isFav = favourites.contains(m.country)
        holder.bookmarkBtn.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(ctx, if (isFav) R.color.colorPrimary else R.color.text_minor)
        )

        holder.itemView.setOnClickListener { onItemClick(m) }
        holder.itemView.setOnLongClickListener { onItemLongClick(m); true }
        holder.bookmarkBtn.setOnClickListener { onFavouriteClick(m) }
    }

    fun setSelected(country: String?) {
        val old = selectedCountry
        selectedCountry = country
        if (old != null) {
            val idx = items.indexOfFirst { it is ListItem.Item && it.medallist.country == old }
            if (idx >= 0) notifyItemChanged(idx)
        }
        if (country != null) {
            val idx = items.indexOfFirst { it is ListItem.Item && it.medallist.country == country }
            if (idx >= 0) notifyItemChanged(idx)
        }
    }

    fun update(newItems: List<ListItem>, newFavourites: Set<String>) {
        items = newItems
        favourites = newFavourites
        notifyDataSetChanged()
    }

    fun updateItems(newItems: List<ListItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size
}
