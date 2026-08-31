package com.lucas.horas.history

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.lucas.horas.data.AppDatabase
import com.lucas.horas.data.PunchType
import com.lucas.horas.databinding.ActivityDayDetailBinding
import com.lucas.horas.domain.HoursCalculator
import com.lucas.horas.domain.MessageBuilder
import com.lucas.horas.theme.ThemePainter
import com.lucas.horas.theme.ThemeStore
import com.lucas.horas.util.ShareUtils
import com.lucas.horas.util.TimeUtils
import kotlinx.coroutines.launch

class DayDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DAY_START = "extra_day_start"
    }

    private lateinit var binding: ActivityDayDetailBinding
    private val dao by lazy { AppDatabase.getInstance(this).punchDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDayDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val dayStart = intent.getLongExtra(EXTRA_DAY_START, TimeUtils.startOfToday())
        binding.toolbar.title = TimeUtils.formatDayLabel(dayStart)

        lifecycleScope.launch {
            val dayEnd = TimeUtils.endOfDay(dayStart)
            val punches = dao.getBetween(dayStart, dayEnd)
            val resumo = HoursCalculator.summarizeDay(dayStart, punches)

            binding.txtRegistosDia.text = if (punches.isEmpty()) {
                getString(com.lucas.horas.R.string.sem_registos)
            } else {
                punches.joinToString("\n") { punch ->
                    val label = if (punch.type == PunchType.ENTRADA) "Entrada" else "Saída"
                    val nota = if (!punch.note.isNullOrBlank()) " — ${punch.note}" else ""
                    "$label: ${TimeUtils.formatTime(punch.timestamp)}$nota"
                }
            }

            val sufixo = if (resumo.inProgress) " (em curso)" else ""
            binding.txtTotalDia.text = "Total: ${TimeUtils.formatDuration(resumo.totalMillis)}$sufixo"

            binding.btnEnviarDia.setOnClickListener {
                val texto = MessageBuilder.buildDayMessage(resumo)
                ShareUtils.shareText(this@DayDetailActivity, texto)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val tema = ThemeStore.getSelectedTheme(this) ?: return
        ThemePainter.paintRoot(binding.root, tema)
        ThemePainter.paintStatusBar(this, tema)
        ThemePainter.paintToolbar(binding.toolbar, tema)
        ThemePainter.paintPrimaryText(binding.txtTotalDia, tema)
        ThemePainter.paintSecondaryText(binding.txtRegistosDia, tema)
        ThemePainter.paintFilledButton(binding.btnEnviarDia, tema)
    }
}
