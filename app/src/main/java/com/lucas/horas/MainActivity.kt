package com.lucas.horas

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import com.lucas.horas.ads.AdsProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.lucas.horas.data.AppDatabase
import com.lucas.horas.data.AvisosPrefs
import com.lucas.horas.data.PunchEntity
import com.lucas.horas.data.PunchType
import com.lucas.horas.databinding.ActivityMainBinding
import com.lucas.horas.domain.HoursCalculator
import com.lucas.horas.domain.MessageBuilder
import com.lucas.horas.history.DayDetailActivity
import com.lucas.horas.history.HistoryActivity
import com.lucas.horas.history.PunchAdapter
import com.lucas.horas.history.PunchEditor
import com.lucas.horas.theme.ThemePainter
import com.lucas.horas.theme.ThemeStore
import com.lucas.horas.util.ShareUtils
import com.lucas.horas.util.TimeUtils
import com.lucas.horas.widget.WidgetUpdater
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val dao by lazy { AppDatabase.getInstance(this).punchDao() }
    private val adapterHoje = PunchAdapter { punch -> PunchEditor.open(this, punch, dao) { carregarHoje() } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.txtRelogio.text = TimeUtils.formatTime(System.currentTimeMillis())

        binding.recyclerHoje.layoutManager = LinearLayoutManager(this)
        binding.recyclerHoje.adapter = adapterHoje

        binding.btnEntrada.setOnClickListener { registarPonto(PunchType.ENTRADA) }
        binding.btnSaida.setOnClickListener { tentarRegistarSaida() }

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

        AdsProvider.prepareAndShowBanner(this, binding.adContainer)
    }

    override fun onResume() {
        super.onResume()
        binding.txtRelogio.text = TimeUtils.formatTime(System.currentTimeMillis())
        carregarHoje()
        aplicarTema()
    }

    private fun aplicarTema() {
        adapterHoje.tema = ThemeStore.getSelectedTheme(this)
        adapterHoje.notifyDataSetChanged()

        val tema = ThemeStore.getSelectedTheme(this) ?: return
        ThemePainter.paintRoot(binding.root, tema)
        ThemePainter.paintStatusBar(this, tema)
        ThemePainter.paintEntradaButton(binding.btnEntrada, tema)
        ThemePainter.paintSaidaButton(binding.btnSaida, tema)
        ThemePainter.paintPrimaryText(binding.txtRelogio, tema)
        ThemePainter.paintPrimaryText(binding.txtTituloHoje, tema)
        ThemePainter.paintSecondaryText(binding.txtSemRegistosHoje, tema)
        ThemePainter.paintPrimaryText(binding.txtTotalHoje, tema)
        ThemePainter.paintInput(binding.layoutNota, binding.editNota, tema)
        ThemePainter.paintOutlinedOrTextButton(binding.btnEnviarHoje, tema)
        ThemePainter.paintOutlinedOrTextButton(binding.btnEscolherDia, tema)
        ThemePainter.paintIcon(binding.btnHistorico, tema)
        ThemePainter.paintIcon(binding.btnWifiConfig, tema)
        ThemePainter.paintIcon(binding.btnTema, tema)
        ThemePainter.paintIcon(binding.btnIdioma, tema)
    }

    private fun tentarRegistarSaida() {
        if (!AvisosPrefs.isAvisoSaidaSemEntradaAtivo(this)) {
            registarPonto(PunchType.SAIDA)
            return
        }
        lifecycleScope.launch {
            val inicio = TimeUtils.startOfToday()
            val fim = TimeUtils.endOfDay(inicio)
            val punches = dao.getBetween(inicio, fim)
            val resumo = HoursCalculator.summarizeDay(inicio, punches)

            if (resumo.inProgress) {
                registarPonto(PunchType.SAIDA)
            } else {
                mostrarAvisoSaidaSemEntrada()
            }
        }
    }

    private fun mostrarAvisoSaidaSemEntrada() {
        val checkbox = CheckBox(this).apply {
            text = getString(R.string.nao_perguntar_novamente)
            setPadding(60, 20, 60, 0)
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.aviso_saida_sem_entrada_titulo)
            .setMessage(R.string.aviso_saida_sem_entrada_texto)
            .setView(checkbox)
            .setPositiveButton(R.string.btn_registar) { _, _ ->
                if (checkbox.isChecked) {
                    AvisosPrefs.setAvisoSaidaSemEntradaAtivo(this, false)
                }
                registarPonto(PunchType.SAIDA)
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
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
            WidgetUpdater.updateAll(this@MainActivity)
        }
    }

    private fun carregarHoje() {
        lifecycleScope.launch {
            val inicio = TimeUtils.startOfToday()
            val fim = TimeUtils.endOfDay(inicio)
            val punches = dao.getBetween(inicio, fim)
            val resumo = HoursCalculator.summarizeDay(inicio, punches)

            adapterHoje.submitList(punches)
            binding.txtSemRegistosHoje.visibility = if (punches.isEmpty()) View.VISIBLE else View.GONE

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
