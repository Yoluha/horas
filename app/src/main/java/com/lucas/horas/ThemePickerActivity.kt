package com.lucas.horas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.lucas.horas.databinding.ActivityThemePickerBinding
import com.lucas.horas.theme.ALL_THEMES
import com.lucas.horas.theme.ThemeListItem
import com.lucas.horas.theme.ThemePainter
import com.lucas.horas.theme.ThemePickerAdapter
import com.lucas.horas.theme.ThemeStore

class ThemePickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityThemePickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThemePickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val items = mutableListOf<ThemeListItem>(ThemeListItem.Row(null))
        for ((categoria, temas) in ALL_THEMES.groupBy { it.category }) {
            items.add(ThemeListItem.Header(categoria))
            items.addAll(temas.map { ThemeListItem.Row(it) })
        }

        binding.recyclerTemas.layoutManager = LinearLayoutManager(this)
        binding.recyclerTemas.adapter = ThemePickerAdapter(items, ThemeStore.getSelectedThemeId(this)) { tema ->
            ThemeStore.setSelectedThemeId(this, tema?.id)
        }
    }

    override fun onResume() {
        super.onResume()
        val tema = ThemeStore.getSelectedTheme(this) ?: return
        ThemePainter.paintRoot(binding.root, tema)
        ThemePainter.paintToolbar(binding.toolbar, tema)
    }
}
