package com.lucas.horas.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.core.content.FileProvider
import com.lucas.horas.R
import java.io.File
import java.io.FileOutputStream

object ShareUtils {

    /** Abre o seletor de partilha do Android com o texto pronto (WhatsApp, SMS, etc — à escolha). */
    fun shareText(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        abrirChooser(context, intent)
    }

    /** Guarda o bitmap em cache e abre o seletor de partilha do Android como imagem. */
    fun shareImage(context: Context, bitmap: Bitmap, fileName: String) {
        val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(imagesDir, fileName)
        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        abrirChooser(context, intent)
    }

    private fun abrirChooser(context: Context, intent: Intent) {
        try {
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.enviar_via)))
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.erro_partilha), Toast.LENGTH_LONG).show()
        }
    }
}
