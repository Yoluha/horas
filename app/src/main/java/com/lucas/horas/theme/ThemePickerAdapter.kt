package com.lucas.horas.theme

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lucas.horas.databinding.ItemThemeHeaderBinding
import com.lucas.horas.databinding.ItemThemeRowBinding

sealed class ThemeListItem {
    data class Header(val title: String) : ThemeListItem()
    data class Row(val theme: AppTheme?) : ThemeListItem() // theme == null -> "Automático"
}

class ThemePickerAdapter(
    private val items: List<ThemeListItem>,
    private var selectedId: String?,
    private val onSelect: (AppTheme?) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ROW = 1
    }

    override fun getItemViewType(position: Int): Int =
        if (items[position] is ThemeListItem.Header) TYPE_HEADER else TYPE_ROW

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderVH(ItemThemeHeaderBinding.inflate(inflater, parent, false))
        } else {
            RowVH(ItemThemeRowBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ThemeListItem.Header -> (holder as HeaderVH).bind(item.title)
            is ThemeListItem.Row -> (holder as RowVH).bind(item.theme)
        }
    }

    override fun getItemCount(): Int = items.size

    private inner class HeaderVH(val binding: ItemThemeHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.root.text = title
        }
    }

    private inner class RowVH(val binding: ItemThemeRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(theme: AppTheme?) {
            val id = theme?.id
            binding.txtLabel.text = theme?.label ?: binding.root.context.getString(com.lucas.horas.R.string.tema_automatico)

            if (theme != null) {
                binding.root.setBackgroundColor(theme.bgCardColor)
                binding.viewAccent.setBackgroundColor(theme.accentColor)
                binding.txtLabel.setTextColor(theme.textMainColor)
                binding.txtCheck.setTextColor(theme.accentColor)
            } else {
                binding.root.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                binding.viewAccent.setBackgroundColor(android.graphics.Color.GRAY)
                binding.txtLabel.setTextColor(binding.root.context.getColor(com.lucas.horas.R.color.text_primary))
                binding.txtCheck.setTextColor(binding.root.context.getColor(com.lucas.horas.R.color.text_primary))
            }

            binding.txtCheck.visibility = if (id == selectedId) android.view.View.VISIBLE else android.view.View.INVISIBLE

            binding.root.setOnClickListener {
                val previous = selectedId
                selectedId = id
                notifyItemChanged(items.indexOfFirst { it is ThemeListItem.Row && it.theme?.id == previous })
                notifyItemChanged(bindingAdapterPosition)
                onSelect(theme)
            }
        }
    }
}
