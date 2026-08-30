package com.lucas.horas.theme

import android.graphics.PorterDuff
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

/**
 * Pinta manualmente as cores do tema escolhido nas views principais de cada ecrã.
 * As cores semânticas (verde=Entrada, vermelho=Saída) NUNCA são tocadas por aqui —
 * tal como no 3D Manager, --success/--danger ficam fixas em todos os temas.
 */
object ThemePainter {

    fun paintRoot(view: View, theme: AppTheme) {
        view.setBackgroundColor(theme.bgBodyColor)
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
        button.setTextColor(theme.textMainColor)
    }

    fun paintOutlinedOrTextButton(button: MaterialButton, theme: AppTheme) {
        button.setTextColor(theme.accentColor)
        button.strokeColor = android.content.res.ColorStateList.valueOf(theme.accentColor)
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
}
