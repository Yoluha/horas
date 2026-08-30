package com.lucas.horas

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.lucas.horas.data.WifiPrefs
import com.lucas.horas.databinding.ActivityWifiConfigBinding
import com.lucas.horas.service.WifiPresenceService
import com.lucas.horas.theme.ThemePainter
import com.lucas.horas.theme.ThemeStore

class WifiConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWifiConfigBinding

    private val pedirPermissoes = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWifiConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.switchAtivar.isChecked = WifiPrefs.isEnabled(this)
        binding.editSsid.setText(WifiPrefs.getSsid(this))

        binding.btnUsarRedeAtual.setOnClickListener { usarRedeAtual() }
        binding.btnGuardarWifi.setOnClickListener { guardar() }

        pedirPermissoesNecessarias()
    }

    private fun pedirPermissoesNecessarias() {
        val permissoes = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissoes.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val faltam = permissoes.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (faltam.isNotEmpty()) {
            pedirPermissoes.launch(faltam.toTypedArray())
        }
    }

    private fun usarRedeAtual() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, R.string.erro_permissao_localizacao, Toast.LENGTH_LONG).show()
            pedirPermissoesNecessarias()
            return
        }

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ssid = wifiManager.connectionInfo?.ssid?.trim('"').orEmpty()

        if (ssid.isBlank() || ssid == "<unknown ssid>") {
            Toast.makeText(this, R.string.erro_sem_rede_atual, Toast.LENGTH_LONG).show()
        } else {
            binding.editSsid.setText(ssid)
        }
    }

    private fun guardar() {
        val ssid = binding.editSsid.text?.toString()?.trim().orEmpty()
        val ativar = binding.switchAtivar.isChecked

        if (ativar && ssid.isBlank()) {
            Toast.makeText(this, R.string.erro_sem_rede_atual, Toast.LENGTH_LONG).show()
            return
        }

        WifiPrefs.setSsid(this, ssid)
        WifiPrefs.setEnabled(this, ativar)

        val intentServico = Intent(this, WifiPresenceService::class.java)
        if (ativar) {
            ContextCompat.startForegroundService(this, intentServico)
        } else {
            stopService(intentServico)
        }

        Toast.makeText(this, R.string.horario_guardado, Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onResume() {
        super.onResume()
        val tema = ThemeStore.getSelectedTheme(this) ?: return
        ThemePainter.paintRoot(binding.root, tema)
        ThemePainter.paintToolbar(binding.toolbar, tema)
        ThemePainter.paintSecondaryText(binding.txtExplicacao, tema)
        ThemePainter.paintSecondaryText(binding.txtAvisoBateria, tema)
        ThemePainter.paintInput(binding.layoutSsid, binding.editSsid, tema)
        ThemePainter.paintOutlinedOrTextButton(binding.btnUsarRedeAtual, tema)
        ThemePainter.paintFilledButton(binding.btnGuardarWifi, tema)
        binding.switchAtivar.setTextColor(tema.textMainColor)
    }
}
