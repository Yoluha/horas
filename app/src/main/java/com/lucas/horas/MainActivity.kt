package com.lucas.horas

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.lucas.horas.data.AppDatabase
import com.lucas.horas.data.PunchEntity
import com.lucas.horas.data.PunchType
import com.lucas.horas.databinding.ActivityMainBinding
import com.lucas.horas.domain.HoursCalculator
import com.lucas.horas.domain.MessageBuilder
import com.lucas.horas.history.DayDetailActivity
import com.lucas.horas.history.HistoryActivity
import com.lucas.horas.theme.ThemePainter
import com.lucas.horas.theme.ThemeStore
import com.lucas.horas.util.ShareUtils
import com.lucas.horas.util.TimeUtils
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val dao by lazy { AppDatabase.getInstance(this).punchDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.txtRelogio.text = TimeUtils.formatTime(System.currentTimeMillis())

        binding.btnEntrada.setOnClickListener { registarPonto(PunchType.ENTRADA) }
        binding.btnSaida.setOnClickListener { registarPonto(PunchType.SAIDA) }

        binding.btnEnviarHoje.setOnClickListener { enviarHoje() }
        binding.btnEscolherDia.setOnClickListener { abrirSeletorDeData() }
        binding.btnHistorico.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.btnWifiConfig.setOnClickListener {
            startActivity(Intent(this, WifiConfigActivity::class.java))
        }
        binding.btnTema.setOnClickListener {
            startActivity(Intent(this, ThemePickerActivity::class.java))
        }
        binding.btnIdioma.setOnClickListener {
            startActivity(Intent(this, LanguageActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        binding.txtRelogio.text = TimeUtils.formatTime(System.currentTimeMillis())
        carregarHoje()
        aplicarTema()
    }

    private fun aplicarTema() {
        val tema = ThemeStore.getSelectedTheme(this) ?: return
        ThemePainter.paintRoot(binding.root, tema)
        ThemePainter.paintPrimaryText(binding.txtRelogio, tema)
        ThemePainter.paintPrimaryText(binding.txtTituloHoje, tema)
        ThemePainter.paintSecondaryText(binding.txtRegistosHoje, tema)
        ThemePainter.paintPrimaryText(binding.txtTotalHoje, tema)
        ThemePainter.paintInput(binding.layoutNota, binding.editNota, tema)
        ThemePainter.paintOutlinedOrTextButton(binding.btnEnviarHoje, tema)
        ThemePainter.paintOutlinedOrTextButton(binding.btnEscolherDia, tema)
        ThemePainter.paintIcon(binding.btnHistorico, tema)
        ThemePainter.paintIcon(binding.btnWifiConfig, tema)
        ThemePainter.paintIcon(binding.btnTema, tema)
        ThemePainter.paintIcon(binding.btnIdioma, tema)
    }

    private fun registarPonto(type: PunchType) {
        val nota = binding.editNota.text?.toString()?.trim().orEmpty()
        lifecycleScope.launch {
            dao.insert(
                PunchEntity(
                    timestamp = System.currentTimeMillis(),
                    type = type,
                    note = if (nota.isBlank()) null else nota
                )
            )
            binding.editNota.setText("")
            carregarHoje()
        }
    }

    private fun carregarHoje() {
        lifecycleScope.launch {
            val inicio = TimeUtils.startOfToday()
            val fim = TimeUtils.endOfDay(inicio)
            val punches = dao.getBetween(inicio, fim)
            val resumo = HoursCalculator.summarizeDay(inicio, punches)

            if (punches.isEmpty()) {
                binding.txtRegistosHoje.text = getString(R.string.sem_registos)
            } else {
                binding.txtRegistosHoje.text = punches.joinToString("\n") { punch ->
                    val label = if (punch.type == PunchType.ENTRADA) "Entrada" else "Saída"
                    val nota = if (!punch.note.isNullOrBlank()) " — ${punch.note}" else ""
                    "$label: ${TimeUtils.formatTime(punch.timestamp)}$nota"
                }
            }

            val sufixo = if (resumo.inProgress) " ${getString(R.string.em_curso)}" else ""
            binding.txtTotalHoje.text = "Total: ${TimeUtils.formatDuration(resumo.totalMillis)}$sufixo"
        }
    }

    private fun enviarHoje() {
        lifecycleScope.launch {
            val inicio = TimeUtils.startOfToday()
            val fim = TimeUtils.endOfDay(inicio)
            val punches = dao.getBetween(inicio, fim)
            val resumo = HoursCalculator.summarizeDay(inicio, punches)
            val texto = MessageBuilder.buildDayMessage(resumo)
            ShareUtils.shareText(this@MainActivity, texto)
        }
    }

    private fun abrirSeletorDeData() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, ano, mes, dia ->
                val escolhido = Calendar.getInstance()
                escolhido.set(ano, mes, dia, 0, 0, 0)
                escolhido.set(Calendar.MILLISECOND, 0)
                val intent = Intent(this, DayDetailActivity::class.java)
                intent.putExtra(DayDetailActivity.EXTRA_DAY_START, TimeUtils.startOfDay(escolhido.timeInMillis))
                startActivity(intent)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
