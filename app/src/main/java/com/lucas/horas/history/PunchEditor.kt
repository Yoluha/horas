package com.lucas.horas.history

import android.app.Activity
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.lucas.horas.R
import com.lucas.horas.data.PunchDao
import com.lucas.horas.data.PunchEntity
import com.lucas.horas.util.TimeUtils
import com.lucas.horas.widget.WidgetUpdater
import kotlinx.coroutines.launch
import java.util.Calendar

/** Diálogo partilhado para editar/apagar um registo — usado no ecrã "Hoje" e no detalhe de qualquer dia. */
object PunchEditor {

    fun open(activity: androidx.appcompat.app.AppCompatActivity, punch: PunchEntity, dao: PunchDao, onDone: () -> Unit) {
        val container = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 20, 60, 20)
        }

        var novaHora = punch.timestamp

        val txtHora = TextView(activity).apply {
            text = "${activity.getString(R.string.btn_alterar_hora)}: ${TimeUtils.formatTime(novaHora)}"
            textSize = 16f
            setPadding(0, 20, 0, 20)
            isClickable = true
            setOnClickListener {
                val cal = Calendar.getInstance().apply { timeInMillis = novaHora }
                TimePickerDialog(
                    activity,
                    { _, hora, minuto ->
                        val novoCal = Calendar.getInstance().apply { timeInMillis = novaHora }
                        novoCal.set(Calendar.HOUR_OF_DAY, hora)
                        novoCal.set(Calendar.MINUTE, minuto)
                        novaHora = novoCal.timeInMillis
                        text = "${activity.getString(R.string.btn_alterar_hora)}: ${TimeUtils.formatTime(novaHora)}"
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
                ).show()
            }
        }

        val editNota = EditText(activity).apply {
            setText(punch.note.orEmpty())
            hint = activity.getString(R.string.hint_nota)
        }

        container.addView(txtHora)
        container.addView(editNota)

        AlertDialog.Builder(activity)
            .setTitle(R.string.titulo_editar_registo)
            .setView(container)
            .setPositiveButton(R.string.btn_guardar) { _, _ ->
                val notaTexto = editNota.text?.toString()?.trim().orEmpty()
                activity.lifecycleScope.launch {
                    dao.update(punch.copy(timestamp = novaHora, note = if (notaTexto.isBlank()) null else notaTexto))
                    Toast.makeText(activity, R.string.registo_atualizado, Toast.LENGTH_SHORT).show()
                    WidgetUpdater.updateAll(activity)
                    onDone()
                }
            }
            .setNeutralButton(R.string.btn_apagar) { _, _ -> confirmarApagar(activity, punch, dao, onDone) }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }

    private fun confirmarApagar(activity: Activity, punch: PunchEntity, dao: PunchDao, onDone: () -> Unit) {
        AlertDialog.Builder(activity)
            .setMessage(R.string.confirmar_apagar)
            .setPositiveButton(R.string.btn_apagar) { _, _ ->
                (activity as androidx.appcompat.app.AppCompatActivity).lifecycleScope.launch {
                    dao.delete(punch)
                    Toast.makeText(activity, R.string.registo_apagado, Toast.LENGTH_SHORT).show()
                    WidgetUpdater.updateAll(activity)
                    onDone()
                }
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }
}
