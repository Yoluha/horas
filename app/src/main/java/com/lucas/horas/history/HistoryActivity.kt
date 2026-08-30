package com.lucas.horas.history

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.lucas.horas.ScheduleActivity
import com.lucas.horas.data.AppDatabase
import com.lucas.horas.databinding.ActivityHistoryBinding
import com.lucas.horas.domain.DaySummary
import com.lucas.horas.domain.HoursCalculator
import com.lucas.horas.theme.ThemePainter
import com.lucas.horas.theme.ThemeStore
import com.lucas.horas.util.HistoryImageRenderer
import com.lucas.horas.util.ShareUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val dao by lazy { AppDatabase.getInstance(this).punchDao() }
    private var diasAtuais: List<DaySummary> = emptyList()

    private val adapter = DayAdapter { day ->
        val intent = Intent(this, DayDetailActivity::class.java)
        intent.putExtra(DayDetailActivity.EXTRA_DAY_START, day.dayStartMillis)
        startActivity(intent)
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
                Toast.makeText(this, com.lucas.horas.R.string.sem_historico, Toast.LENGTH_SHORT).show()
            } else {
                val bitmap = HistoryImageRenderer.buildResumo(this, diasAtuais)
                ShareUtils.shareImage(this, bitmap, "resumo_horas.png")
            }
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
        ThemePainter.paintToolbar(binding.toolbar, tema)
        ThemePainter.paintIcon(binding.btnHorario, tema)
        ThemePainter.paintSecondaryText(binding.txtVazio, tema)
        ThemePainter.paintFilledButton(binding.btnExportarResumo, tema)
    }
}
