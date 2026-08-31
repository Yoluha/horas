package com.lucas.horas.theme

import android.app.Activity
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

/**
 * Pinta manualmente as cores do tema escolhido nas views principais de cada ecrã,
 * incluindo os botões Entrada/Saída e a barra de estado do sistema.
 */
object ThemePainter {

    fun paintRoot(view: View, theme: AppTheme) {
        view.setBackgroundColor(theme.bgBodyColor)
    }

    fun paintStatusBar(activity: Activity, theme: AppTheme) {
        activity.window.statusBarColor = theme.bgCardColor
        val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        controller.isAppearanceLightStatusBars = isLight(theme.bgCardColor)
    }

    fun paintToolbar(toolbar: Toolbar, theme: AppTheme) {
        toolbar.setBackgroundColor(theme.bgCardColor)
        toolbar.setTitleTextColor(theme.textMainColor)
        toolbar.navigationIcon?.setColorFilter(theme.textMainColor, PorterDuff.Mode.SRC_IN)
    }

    fun paintPrimaryText(view: TextView, theme: AppTheme) {
        view.setTextColor(theme.textMainColor)
    }

    fun paintSecondaryText(view: TextView, theme: AppTheme) {
        view.setTextColor(theme.textSecColor)
    }

    fun paintFilledButton(button: MaterialButton, theme: AppTheme) {
        button.backgroundTintList = android.content.res.ColorStateList.valueOf(theme.accentColor)
        button.setTextColor(contrastColor(theme.accentColor))
    }

    fun paintOutlinedOrTextButton(button: MaterialButton, theme: AppTheme) {
        button.setTextColor(theme.accentColor)
        button.strokeColor = android.content.res.ColorStateList.valueOf(theme.accentColor)
    }

    /** Entrada usa a cor de destaque do tema; Saída usa a cor de contorno — mantém as duas distintas. */
    fun paintEntradaButton(button: MaterialButton, theme: AppTheme) {
        button.backgroundTintList = android.content.res.ColorStateList.valueOf(theme.accentColor)
        button.setTextColor(contrastColor(theme.accentColor))
    }

    fun paintSaidaButton(button: MaterialButton, theme: AppTheme) {
        button.backgroundTintList = android.content.res.ColorStateList.valueOf(theme.borderColor)
        button.setTextColor(contrastColor(theme.borderColor))
    }

    fun paintInput(inputLayout: TextInputLayout, editText: EditText, theme: AppTheme) {
        inputLayout.boxBackgroundColor = theme.bgInputColor
        inputLayout.boxStrokeColor = theme.borderColor
        inputLayout.defaultHintTextColor = android.content.res.ColorStateList.valueOf(theme.textSecColor)
        editText.setTextColor(theme.textMainColor)
    }

    fun paintIcon(imageButton: ImageButton, theme: AppTheme) {
        imageButton.setColorFilter(theme.textMainColor, PorterDuff.Mode.SRC_IN)
    }

    private fun isLight(color: Int): Boolean {
        val r = Color.red(color) / 255.0
        val g = Color.green(color) / 255.0
        val b = Color.blue(color) / 255.0
        val luminance = 0.299 * r + 0.587 * g + 0.114 * b
        return luminance > 0.6
    }

    private fun contrastColor(bg: Int): Int = if (isLight(bg)) Color.BLACK else Color.WHITE
}
