package com.lucas.horas.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lucas.horas.R
import com.lucas.horas.data.PunchEntity
import com.lucas.horas.data.PunchType
import com.lucas.horas.databinding.ItemPunchBinding
import com.lucas.horas.theme.AppTheme
import com.lucas.horas.util.TimeUtils

class PunchAdapter(
    private val onClick: (PunchEntity) -> Unit
) : RecyclerView.Adapter<PunchAdapter.ViewHolder>() {

    private var punches: List<PunchEntity> = emptyList()
    var tema: AppTheme? = null

    fun submitList(newPunches: List<PunchEntity>) {
        punches = newPunches
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPunchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(punches[position])
    }

    override fun getItemCount(): Int = punches.size

    inner class ViewHolder(private val binding: ItemPunchBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(punch: PunchEntity) {
            val context = binding.root.context
            val isEntrada = punch.type == PunchType.ENTRADA
            binding.txtTipo.text = if (isEntrada) context.getString(R.string.btn_entrada) else context.getString(R.string.btn_saida)
            binding.txtHora.text = TimeUtils.formatTime(punch.timestamp)

            if (!punch.note.isNullOrBlank()) {
                binding.txtNota.text = punch.note
                binding.txtNota.visibility = View.VISIBLE
            } else {
                binding.txtNota.visibility = View.GONE
            }

            val t = tema
            if (t != null) {
                binding.txtTipo.setTextColor(if (isEntrada) t.accentColor else t.borderColor)
                binding.txtHora.setTextColor(t.textMainColor)
                binding.txtNota.setTextColor(t.textSecColor)
            } else {
                binding.txtTipo.setTextColor(
                    context.getColor(if (isEntrada) R.color.entrada_green else R.color.saida_red)
                )
            }

            binding.root.setOnClickListener { onClick(punch) }
        }
    }
}
