package com.lucas.horas.history

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.lucas.horas.R
import com.lucas.horas.ScheduleActivity
import com.lucas.horas.data.AppDatabase
import com.lucas.horas.data.BackupManager
import com.lucas.horas.databinding.ActivityHistoryBinding
import com.lucas.horas.domain.DaySummary
import com.lucas.horas.domain.HoursCalculator
import com.lucas.horas.theme.ThemePainter
import com.lucas.horas.theme.ThemeStore
import com.lucas.horas.util.HistoryImageRenderer
import com.lucas.horas.util.ShareUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val dao by lazy { AppDatabase.getInstance(this).punchDao() }
    private var diasAtuais: List<DaySummary> = emptyList()

    private val adapter = DayAdapter { day ->
        val intent = Intent(this, DayDetailActivity::class.java)
        intent.putExtra(DayDetailActivity.EXTRA_DAY_START, day.dayStartMillis)
        startActivity(intent)
    }

    private val criarFicheiroExport = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) exportarPara(uri)
    }

    private val escolherFicheiroImport = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) confirmarImportar(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.recyclerDias.layoutManager = LinearLayoutManager(this)
        binding.recyclerDias.adapter = adapter

        binding.btnHorario.setOnClickListener {
            startActivity(Intent(this, ScheduleActivity::class.java))
        }

        binding.btnExportarResumo.setOnClickListener {
            if (diasAtuais.isEmpty()) {
                Toast.makeText(this, R.string.sem_historico, Toast.LENGTH_SHORT).show()
            } else {
                val bitmap = HistoryImageRenderer.buildResumo(this, diasAtuais)
                ShareUtils.shareImage(this, bitmap, "resumo_horas.png")
            }
        }

        binding.btnExportarDados.setOnClickListener {
            val nomeFicheiro = "horas-backup-${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(System.currentTimeMillis())}.json"
            criarFicheiroExport.launch(nomeFicheiro)
        }

        binding.btnImportarDados.setOnClickListener {
            escolherFicheiroImport.launch(arrayOf("application/json", "text/plain", "text/*", "*/*"))
        }

        lifecycleScope.launch {
            dao.observeAll().collectLatest { punches ->
                val dias = HoursCalculator.groupByDay(punches)
                diasAtuais = dias
                adapter.submitList(dias)
                binding.txtVazio.visibility = if (dias.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
        }
    }

    private fun exportarPara(uri: Uri) {
        lifecycleScope.launch {
            try {
                val punches = dao.getAllOnce()
                val json = BackupManager.buildJson(punches, this@HistoryActivity)
                contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(this@HistoryActivity, R.string.exportar_sucesso, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@HistoryActivity, R.string.erro_exportar, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun confirmarImportar(uri: Uri) {
        AlertDialog.Builder(this)
            .setMessage(R.string.confirmar_importar)
            .setPositiveButton(R.string.btn_importar_dados) { _, _ -> importarDe(uri) }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }

    private fun importarDe(uri: Uri) {
        lifecycleScope.launch {
            try {
                val texto = contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                    ?: throw IllegalStateException("ficheiro vazio")
                val punches = BackupManager.parsePunches(texto)
                dao.deleteAll()
                dao.insertAll(punches)
                BackupManager.aplicarHorario(texto, this@HistoryActivity)
                Toast.makeText(this@HistoryActivity, R.string.importar_sucesso, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@HistoryActivity, R.string.erro_importar, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        aplicarTema()
    }

    private fun aplicarTema() {
        val tema = ThemeStore.getSelectedTheme(this)
        adapter.tema = tema
        adapter.notifyDataSetChanged()
        if (tema == null) return

        ThemePainter.paintRoot(binding.root, tema)
        ThemePainter.paintStatusBar(this, tema)
        ThemePainter.paintToolbar(binding.toolbar, tema)
        ThemePainter.paintIcon(binding.btnHorario, tema)
        ThemePainter.paintSecondaryText(binding.txtVazio, tema)
        ThemePainter.paintFilledButton(binding.btnExportarResumo, tema)
        ThemePainter.paintOutlinedOrTextButton(binding.btnExportarDados, tema)
        ThemePainter.paintOutlinedOrTextButton(binding.btnImportarDados, tema)
    }
}
