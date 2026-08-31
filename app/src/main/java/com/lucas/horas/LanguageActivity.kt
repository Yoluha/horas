package com.lucas.horas

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.lucas.horas.databinding.ActivityLanguageBinding
import com.lucas.horas.theme.ThemePainter
import com.lucas.horas.theme.ThemeStore

class LanguageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageBinding
    private val rows = mutableListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // code null = segue o idioma do sistema
        val idiomas = listOf(
            null to getString(R.string.idioma_automatico),
            "pt" to "Português",
            "en" to "English",
            "es" to "Español",
            "fr" to "Français",
            "de" to "Deutsch",
            "it" to "Italiano",
            "pl" to "Polski",
            "ja" to "日本語",
            "nl" to "Nederlands",
            "cs" to "Čeština",
            "zh" to "中文",
            "ru" to "Русский",
            "ko" to "한국어",
            "ne" to "नेपाली"
        )

        val atual = AppCompatDelegate.getApplicationLocales()
        val codigoAtual = if (atual.isEmpty) null else atual[0]?.language

        for ((codigo, nome) in idiomas) {
            val row = TextView(this).apply {
                text = if (codigo == codigoAtual) "✓ $nome" else nome
                textSize = 17f
                setTextColor(getColor(R.color.text_primary))
                gravity = Gravity.CENTER_VERTICAL
                setPadding(48, 40, 48, 40)
                isClickable = true
                isFocusable = true
                val outValue = TypedValue()
                theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
                setBackgroundResource(outValue.resourceId)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            row.setOnClickListener {
                val locales = if (codigo == null) {
                    LocaleListCompat.getEmptyLocaleList()
                } else {
                    LocaleListCompat.forLanguageTags(codigo)
                }
                AppCompatDelegate.setApplicationLocales(locales)
            }

            binding.layoutIdiomas.addView(row)
            rows.add(row)
        }
    }

    override fun onResume() {
        super.onResume()
        val tema = ThemeStore.getSelectedTheme(this) ?: return
        ThemePainter.paintRoot(binding.root, tema)
        ThemePainter.paintStatusBar(this, tema)
        ThemePainter.paintToolbar(binding.toolbar, tema)
        rows.forEach { it.setTextColor(tema.textMainColor) }
    }
}
