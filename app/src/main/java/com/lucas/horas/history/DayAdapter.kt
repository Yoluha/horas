package com.lucas.horas.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lucas.horas.databinding.ItemDayBinding
import com.lucas.horas.domain.DaySummary
import com.lucas.horas.theme.AppTheme
import com.lucas.horas.util.TimeUtils

class DayAdapter(
    private val onClick: (DaySummary) -> Unit
) : RecyclerView.Adapter<DayAdapter.ViewHolder>() {

    private var days: List<DaySummary> = emptyList()
    var tema: AppTheme? = null

    fun submitList(newDays: List<DaySummary>) {
        days = newDays
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount(): Int = days.size

    inner class ViewHolder(private val binding: ItemDayBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(day: DaySummary) {
            binding.txtData.text = TimeUtils.formatDayLabel(day.dayStartMillis)
            val sufixo = if (day.inProgress) " (em curso)" else ""
            binding.txtTotal.text = "Total: ${TimeUtils.formatDuration(day.totalMillis)}$sufixo"
            binding.root.setOnClickListener { onClick(day) }

            val t = tema
            if (t != null) {
                binding.root.setBackgroundColor(t.bgCardColor)
                binding.txtData.setTextColor(t.textMainColor)
                binding.txtTotal.setTextColor(t.textSecColor)
            }
        }
    }
}
