package com.lucas.horas.history

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.lucas.horas.R
import com.lucas.horas.data.AppDatabase
import com.lucas.horas.data.PunchEntity
import com.lucas.horas.databinding.ActivityDayDetailBinding
import com.lucas.horas.domain.HoursCalculator
import com.lucas.horas.domain.MessageBuilder
import com.lucas.horas.theme.ThemePainter
import com.lucas.horas.theme.ThemeStore
import com.lucas.horas.util.ShareUtils
import com.lucas.horas.util.TimeUtils
import kotlinx.coroutines.launch
import java.util.Calendar

class DayDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DAY_START = "extra_day_start"
    }

    private lateinit var binding: ActivityDayDetailBinding
    private val dao by lazy { AppDatabase.getInstance(this).punchDao() }
    private var dayStart: Long = 0

    private val adapter = PunchAdapter { punch -> abrirEdicao(punch) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDayDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        dayStart = intent.getLongExtra(EXTRA_DAY_START, TimeUtils.startOfToday())
        binding.toolbar.title = TimeUtils.formatDayLabel(dayStart)

        binding.recyclerPunches.layoutManager = LinearLayoutManager(this)
        binding.recyclerPunches.adapter = adapter

        carregarDia()
    }

    private fun carregarDia() {
        lifecycleScope.launch {
            val dayEnd = TimeUtils.endOfDay(dayStart)
            val punches = dao.getBetween(dayStart, dayEnd)
            val resumo = HoursCalculator.summarizeDay(dayStart, punches)

            adapter.submitList(punches)
            binding.txtSemRegistos.visibility = if (punches.isEmpty()) View.VISIBLE else View.GONE

            val sufixo = if (resumo.inProgress) " (em curso)" else ""
            binding.txtTotalDia.text = "Total: ${TimeUtils.formatDuration(resumo.totalMillis)}$sufixo"

            binding.btnEnviarDia.setOnClickListener {
                val texto = MessageBuilder.buildDayMessage(resumo)
                ShareUtils.shareText(this@DayDetailActivity, texto)
            }
        }
    }

    private fun abrirEdicao(punch: PunchEntity) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 20, 60, 20)
        }

        var novaHora = punch.timestamp

        val txtHora = TextView(this).apply {
            text = "${getString(R.string.btn_alterar_hora)}: ${TimeUtils.formatTime(novaHora)}"
            textSize = 16f
            setPadding(0, 20, 0, 20)
            isClickable = true
            setOnClickListener {
                val cal = Calendar.getInstance().apply { timeInMillis = novaHora }
                TimePickerDialog(
                    this@DayDetailActivity,
                    { _, hora, minuto ->
                        val novoCal = Calendar.getInstance().apply { timeInMillis = novaHora }
                        novoCal.set(Calendar.HOUR_OF_DAY, hora)
                        novoCal.set(Calendar.MINUTE, minuto)
                        novaHora = novoCal.timeInMillis
                        text = "${getString(R.string.btn_alterar_hora)}: ${TimeUtils.formatTime(novaHora)}"
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
                ).show()
            }
        }

        val editNota = EditText(this).apply {
            setText(punch.note.orEmpty())
            hint = getString(R.string.hint_nota)
        }

        container.addView(txtHora)
        container.addView(editNota)

        AlertDialog.Builder(this)
            .setTitle(R.string.titulo_editar_registo)
            .setView(container)
            .setPositiveButton(R.string.btn_guardar) { _, _ ->
                val notaTexto = editNota.text?.toString()?.trim().orEmpty()
                lifecycleScope.launch {
                    dao.update(punch.copy(timestamp = novaHora, note = if (notaTexto.isBlank()) null else notaTexto))
                    Toast.makeText(this@DayDetailActivity, R.string.registo_atualizado, Toast.LENGTH_SHORT).show()
                    carregarDia()
                }
            }
            .setNeutralButton(R.string.btn_apagar) { _, _ -> confirmarApagar(punch) }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }

    private fun confirmarApagar(punch: PunchEntity) {
        AlertDialog.Builder(this)
            .setMessage(R.string.confirmar_apagar)
            .setPositiveButton(R.string.btn_apagar) { _, _ ->
                lifecycleScope.launch {
                    dao.delete(punch)
                    Toast.makeText(this@DayDetailActivity, R.string.registo_apagado, Toast.LENGTH_SHORT).show()
                    carregarDia()
                }
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        adapter.tema = ThemeStore.getSelectedTheme(this)
        adapter.notifyDataSetChanged()

        val tema = ThemeStore.getSelectedTheme(this) ?: return
        ThemePainter.paintRoot(binding.root, tema)
        ThemePainter.paintStatusBar(this, tema)
        ThemePainter.paintToolbar(binding.toolbar, tema)
        ThemePainter.paintPrimaryText(binding.txtTotalDia, tema)
        ThemePainter.paintSecondaryText(binding.txtSemRegistos, tema)
        ThemePainter.paintFilledButton(binding.btnEnviarDia, tema)
    }
}
