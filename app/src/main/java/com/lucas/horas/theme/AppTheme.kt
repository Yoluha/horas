package com.lucas.horas.theme

import android.graphics.Color

data class AppTheme(
    val id: String,
    val label: String,
    val category: String,
    val bgBody: String,
    val bgCard: String,
    val bgInput: String,
    val textMain: String,
    val textSec: String,
    val accent: String,
    val border: String
) {
    val bgBodyColor: Int get() = Color.parseColor(bgBody)
    val bgCardColor: Int get() = Color.parseColor(bgCard)
    val bgInputColor: Int get() = Color.parseColor(bgInput)
    val textMainColor: Int get() = Color.parseColor(textMain)
    val textSecColor: Int get() = Color.parseColor(textSec)
    val accentColor: Int get() = Color.parseColor(accent)
    val borderColor: Int get() = Color.parseColor(border)
}

fun themeById(id: String?): AppTheme? = ALL_THEMES.find { it.id == id }
