package com.lucas.horas.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.lucas.horas.R
import com.lucas.horas.data.AppDatabase
import com.lucas.horas.data.PunchDao
import com.lucas.horas.data.PunchEntity
import com.lucas.horas.data.PunchType
import com.lucas.horas.data.WifiPrefs
import com.lucas.horas.util.TimeUtils
import com.lucas.horas.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class WifiPresenceService : Service() {

    companion object {
        private const val CHANNEL_ID = "wifi_presence"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID_AVISO = "wifi_presence_aviso"
        private const val NOTIFICATION_ID_AVISO = 2
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

            if (!ligadoAgora) {
                avisarDesconexaoSeNecessario(dao, agora)
            }
        }
    }

    /** Ao saíres da rede do trabalho, confirma por notificação se a entrada e a
     * saída de hoje já estão registadas — útil porque a deteção automática pode
     * falhar (bateria, GPS desligado, etc.) e assim não passa despercebido. */
    private suspend fun avisarDesconexaoSeNecessario(dao: PunchDao, horaSaida: Long) {
        if (!WifiPrefs.isAvisoDesconexaoAtivo(applicationContext)) return

        val inicio = TimeUtils.startOfToday()
        val fim = TimeUtils.endOfDay(inicio)
        val punches = dao.getBetween(inicio, fim)
        val entrada = punches.firstOrNull { it.type == PunchType.ENTRADA }

        val linhaEntrada = if (entrada != null) {
            applicationContext.getString(R.string.notif_aviso_entrada_ok, TimeUtils.formatTime(entrada.timestamp))
        } else {
            applicationContext.getString(R.string.notif_aviso_entrada_falta)
        }
        val linhaSaida = applicationContext.getString(R.string.notif_aviso_saida_ok, TimeUtils.formatTime(horaSaida))

        mostrarNotificacaoAviso("$linhaEntrada\n$linhaSaida")
    }

    private fun mostrarNotificacaoAviso(texto: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID_AVISO,
                getString(R.string.notif_canal_aviso_nome),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val notificacao = NotificationCompat.Builder(this, CHANNEL_ID_AVISO)
            .setContentTitle(getString(R.string.notif_aviso_titulo))
            .setContentText(texto.replace("\n", "  ·  "))
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID_AVISO, notificacao)
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
