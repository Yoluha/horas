package com.lucas.horas.theme

import android.content.Context

object ThemeStore {
    private const val PREFS = "horas_prefs"
    private const val KEY_THEME_ID = "app_theme_id"

    /** null = tema automático do sistema (claro/escuro), sem override. */
    fun getSelectedThemeId(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_THEME_ID, null)

    fun setSelectedThemeId(context: Context, id: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_THEME_ID, id).apply()
    }

    fun getSelectedTheme(context: Context): AppTheme? = themeById(getSelectedThemeId(context))
}
