package com.lucas.horas.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.lucas.horas.R
import com.lucas.horas.data.AppDatabase
import com.lucas.horas.data.PunchEntity
import com.lucas.horas.data.PunchType
import com.lucas.horas.data.WifiPrefs
import com.lucas.horas.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class WifiPresenceService : Service() {

    companion object {
        private const val CHANNEL_ID = "wifi_presence"
        private const val NOTIFICATION_ID = 1
        private const val DEBOUNCE_MILLIS = 60_000L
    }

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var connectivityManager: ConnectivityManager
    private var lastAutoEventMillis = 0L

    /** Estado que ACOMPANHAMOS nós, não a rede momentânea — evita o bug de verificar
     * a rede atual depois de já teres saído dela (isso nunca bate certo). */
    private var conectadoAoAlvo = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = reavaliarLigacao()
        override fun onLost(network: Network) = reavaliarLigacao()
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) = reavaliarLigacao()
    }

    override fun onCreate() {
        super.onCreate()
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        startForeground(NOTIFICATION_ID, buildNotification())

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)

        // Sincroniza o estado inicial — se já estiveres ligado à rede-alvo ao ativar
        // esta funcionalidade, isto conta logo como entrada em vez de esperar por um evento.
        reavaliarLigacao()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: IllegalArgumentException) {
            // já não estava registado
        }
        job.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun reavaliarLigacao() {
        val alvo = WifiPrefs.getSsid(applicationContext)
        if (alvo.isBlank()) return

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ssidAtual = wifiManager.connectionInfo?.ssid?.trim('"').orEmpty()
        val ligadoAgora = ssidAtual.equals(alvo, ignoreCase = true)

        if (ligadoAgora == conectadoAoAlvo) return // sem mudança de estado, ignora
        conectadoAoAlvo = ligadoAgora

        val agora = System.currentTimeMillis()
        if (agora - lastAutoEventMillis < DEBOUNCE_MILLIS) return
        lastAutoEventMillis = agora

        val tipo = if (ligadoAgora) PunchType.ENTRADA else PunchType.SAIDA
        scope.launch {
            val dao = AppDatabase.getInstance(applicationContext).punchDao()
            dao.insert(
                PunchEntity(
                    timestamp = agora,
                    type = tipo,
                    note = applicationContext.getString(R.string.nota_auto_wifi)
                )
            )
            WidgetUpdater.updateAll(applicationContext)
        }
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_canal_nome),
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notif_titulo))
            .setContentText(getString(R.string.notif_texto))
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setOngoing(true)
            .build()
    }
}
