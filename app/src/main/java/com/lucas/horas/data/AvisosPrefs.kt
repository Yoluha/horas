package com.lucas.horas.data

import android.content.Context

object AvisosPrefs {
    private const val PREFS = "horas_prefs"
    private const val KEY_AVISO_SAIDA_SEM_ENTRADA = "aviso_saida_sem_entrada"

    fun isAvisoSaidaSemEntradaAtivo(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_AVISO_SAIDA_SEM_ENTRADA, true)

    fun setAvisoSaidaSemEntradaAtivo(context: Context, ativo: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_AVISO_SAIDA_SEM_ENTRADA, ativo)
            .apply()
    }
}
