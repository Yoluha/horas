package com.lucas.horas

import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lucas.horas.data.ScheduleStore
import com.lucas.horas.databinding.ActivityScheduleBinding
import com.lucas.horas.theme.ThemePainter
import com.lucas.horas.theme.ThemeStore
import java.util.Calendar

class ScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleBinding

    private val diasSemana = listOf(
        Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY,
        Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
    )

    private val edits = mutableMapOf<Int, EditText>()
    private val labels = mutableListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val nomesDias = resources.getStringArray(R.array.dias_semana)

        for ((index, diaSemana) in diasSemana.withIndex()) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 16, 0, 16)
            }

            val label = TextView(this).apply {
                text = nomesDias[index]
                textSize = 16f
                setTextColor(getColor(R.color.text_primary))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            labels.add(label)

            val edit = EditText(this).apply {
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                setText(ScheduleStore.getHoursFor(this@ScheduleActivity, diaSemana).toString())
                layoutParams = LinearLayout.LayoutParams(160, LinearLayout.LayoutParams.WRAP_CONTENT)
                gravity = Gravity.END
            }
            edits[diaSemana] = edit

            row.addView(label)
            row.addView(edit)
            binding.layoutDias.addView(row)
        }

        binding.btnGuardarHorario.setOnClickListener {
            for ((diaSemana, edit) in edits) {
                val horas = edit.text?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                ScheduleStore.setHoursFor(this, diaSemana, horas)
            }
            Toast.makeText(this, R.string.horario_guardado, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val tema = ThemeStore.getSelectedTheme(this) ?: return
        ThemePainter.paintRoot(binding.root, tema)
        ThemePainter.paintStatusBar(this, tema)
        ThemePainter.paintToolbar(binding.toolbar, tema)
        ThemePainter.paintFilledButton(binding.btnGuardarHorario, tema)
        ThemePainter.paintSecondaryText(binding.txtExplicacao, tema)
        labels.forEach { ThemePainter.paintPrimaryText(it, tema) }
        edits.values.forEach { it.setTextColor(tema.textMainColor) }
    }
}
