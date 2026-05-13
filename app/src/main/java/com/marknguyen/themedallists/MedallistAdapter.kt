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
    private val onItemClick: (Medallist) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
        holder.nameText.text = m.country
        holder.subtitleText.text = "${m.iocCode} • ${m.timesCompeted} appearances"
        holder.minorText.text = "Gold: ${m.gold}  •  Silver: ${m.silver}  •  Bronze: ${m.bronze}"

        val medalColorRes: Int
        val bgColorRes: Int
        when {
            m.gold > 0 -> { medalColorRes = R.color.medal_gold;   bgColorRes = R.color.bg_gold }
            m.silver > 0 -> { medalColorRes = R.color.medal_silver; bgColorRes = R.color.bg_silver }
            m.bronze > 0 -> { medalColorRes = R.color.medal_bronze; bgColorRes = R.color.bg_bronze }
            else -> { medalColorRes = R.color.text_minor; bgColorRes = R.color.bg_no_medal }
        }

        holder.medalIcon.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(holder.itemView.context, medalColorRes)
        )
        holder.itemView.setBackgroundColor(
            ContextCompat.getColor(holder.itemView.context, bgColorRes)
        )
        holder.itemView.setOnClickListener { onItemClick(m) }
    }

    fun updateItems(newItems: List<ListItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size
}
