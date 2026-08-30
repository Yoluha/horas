package com.lucas.horas.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.lucas.horas.data.ScheduleStore
import com.lucas.horas.domain.DaySummary
import java.text.SimpleDateFormat
import java.util.Locale

object HistoryImageRenderer {

    private const val WIDTH = 1080
    private const val PADDING = 32f
    private const val ROW_HEIGHT = 64f

    private val COL_DATA = PADDING
    private val COL_DIA = PADDING + 150f
    private val COL_HORAS = PADDING + 340f
    private val COL_DIF = PADDING + 560f
    private val COL_NOTA = PADDING + 760f

    fun buildResumo(context: Context, days: List<DaySummary>): Bitmap {
        val ordenados = days.sortedBy { it.dayStartMillis }

        val headerPaint = Paint().apply {
            color = Color.WHITE
            textSize = 30f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val titlePaint = Paint().apply {
            color = Color.parseColor("#212121")
            textSize = 42f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val cellPaint = Paint().apply {
            color = Color.parseColor("#212121")
            textSize = 28f
            isAntiAlias = true
        }
        val positivePaint = Paint(cellPaint).apply { color = Color.parseColor("#2E7D32") }
        val negativePaint = Paint(cellPaint).apply { color = Color.parseColor("#C62828") }
        val headerBgPaint = Paint().apply { color = Color.parseColor("#2E7D32") }
        val rowBgPaint = Paint().apply { color = Color.parseColor("#F5F5F5") }
        val bottomPaint = Paint().apply {
            color = Color.parseColor("#212121")
            textSize = 30f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val titleHeight = 90f
        val headerHeight = ROW_HEIGHT
        val bottomHeight = ROW_HEIGHT + 20f
        val height = (titleHeight + headerHeight + ordenados.size * ROW_HEIGHT + bottomHeight + PADDING).toInt()

        val bitmap = Bitmap.createBitmap(WIDTH, height.coerceAtLeast(300), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        var y = PADDING + 50f
        canvas.drawText("Resumo de Horas", PADDING, y, titlePaint)
        y += 40f

        canvas.drawRect(0f, y, WIDTH.toFloat(), y + headerHeight, headerBgPaint)
        val headerBaseline = y + headerHeight / 2 + 10f
        canvas.drawText("Data", COL_DATA, headerBaseline, headerPaint)
        canvas.drawText("Dia", COL_DIA, headerBaseline, headerPaint)
        canvas.drawText("Horas", COL_HORAS, headerBaseline, headerPaint)
        canvas.drawText("Dif.", COL_DIF, headerBaseline, headerPaint)
        canvas.drawText("Nota", COL_NOTA, headerBaseline, headerPaint)
        y += headerHeight

        var totalMillis = 0L
        var totalDiff = 0L

        val dayFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        val weekdayFormat = SimpleDateFormat("EEE", Locale("pt", "PT"))

        for ((index, day) in ordenados.withIndex()) {
            if (index % 2 == 0) {
                canvas.drawRect(0f, y, WIDTH.toFloat(), y + ROW_HEIGHT, rowBgPaint)
            }
            val baseline = y + ROW_HEIGHT / 2 + 10f

            val expected = ScheduleStore.expectedMillisFor(context, day.dayStartMillis)
            val diff = day.totalMillis - expected
            totalMillis += day.totalMillis
            totalDiff += diff

            val nota = day.punches.mapNotNull { it.note }.filter { it.isNotBlank() }.joinToString("; ")
            val diaSemana = weekdayFormat.format(day.dayStartMillis).replaceFirstChar { it.uppercase() }.removeSuffix(".")

            canvas.drawText(dayFormat.format(day.dayStartMillis), COL_DATA, baseline, cellPaint)
            canvas.drawText(diaSemana, COL_DIA, baseline, cellPaint)
            canvas.drawText(TimeUtils.formatDuration(day.totalMillis), COL_HORAS, baseline, cellPaint)
            canvas.drawText(formatSigned(diff), COL_DIF, baseline, if (diff < 0) negativePaint else positivePaint)
            canvas.drawText(truncar(nota), COL_NOTA, baseline, cellPaint)

            y += ROW_HEIGHT
        }

        canvas.drawRect(0f, y, WIDTH.toFloat(), y + 3f, Paint().apply { color = Color.parseColor("#BDBDBD") })
        y += 20f
        val bottomBaseline = y + ROW_HEIGHT / 2 + 10f
        canvas.drawText("Total: ${TimeUtils.formatDuration(totalMillis)}", COL_DATA, bottomBaseline, bottomPaint)
        canvas.drawText(
            "Dif.: ${formatSigned(totalDiff)}",
            COL_DIF,
            bottomBaseline,
            if (totalDiff < 0) negativePaint else positivePaint
        )

        return bitmap
    }

    private fun formatSigned(millis: Long): String {
        val sinal = if (millis < 0) "-" else "+"
        return "$sinal${TimeUtils.formatDuration(kotlin.math.abs(millis))}"
    }

    private fun truncar(texto: String, max: Int = 22): String =
        if (texto.length > max) texto.take(max - 1) + "…" else texto
}
