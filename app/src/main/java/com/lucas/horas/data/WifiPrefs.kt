package com.lucas.horas.data

import android.content.Context

object WifiPrefs {
    private const val PREFS = "horas_prefs"
    private const val KEY_SSID = "wifi_ssid"
    private const val KEY_ENABLED = "wifi_enabled"
    private const val KEY_AVISO_DESCONEXAO = "wifi_aviso_desconexao"

    fun getSsid(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_SSID, "") ?: ""

    fun setSsid(context: Context, ssid: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_SSID, ssid).apply()
    }

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun isAvisoDesconexaoAtivo(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_AVISO_DESCONEXAO, true)

    fun setAvisoDesconexaoAtivo(context: Context, ativo: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_AVISO_DESCONEXAO, ativo)
            .apply()
    }
}
