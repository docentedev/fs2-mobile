package com.rojas.disco.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.rojas.disco.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class NotificationService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val okHttpClient = OkHttpClient()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val email = intent?.getStringExtra(EXTRA_EMAIL)
        if (email.isNullOrBlank()) {
            stopSelf()
            return START_NOT_STICKY
        }

        createNotificationChannels()

        val foregroundNotification = buildForegroundNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                FOREGROUND_NOTIFICATION_ID,
                foregroundNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(FOREGROUND_NOTIFICATION_ID, foregroundNotification)
        }

        scope.launch {
            listenToTopic(email)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private suspend fun listenToTopic(email: String) {
        val topic = NtfyTopics.generate(email)
        val url = "${BuildConfig.NTFY_BASE_URL.trimEnd('/')}/$topic/sse"

        while (scope.isActive) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("Accept", "text/event-stream")
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use
                    val source = response.body?.source() ?: return@use
                    while (scope.isActive && !source.exhausted()) {
                        val line = source.readUtf8Line() ?: break
                        if (line.startsWith("data:")) {
                            val payload = line.substring(5).trim()
                            if (payload.isNotEmpty()) {
                                handleMessagePayload(payload)
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                // Conexión caída: se reintentará
            } catch (e: Exception) {
                // Error inesperado: se reintentará
            }

            if (scope.isActive) {
                delay(RECONNECT_DELAY_MS)
            }
        }
    }

    private fun handleMessagePayload(payload: String) {
        try {
            val json = JSONObject(payload)
            val message = json.optString("message", "")
            val title = json.optString("title", "Nueva notificación")
            if (message.isNotBlank()) {
                showPushNotification(title, message)
            }
        } catch (e: Exception) {
            // Payload no parseable: ignorar
        }
    }

    private fun showPushNotification(title: String, message: String) {
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val notification = NotificationCompat.Builder(this, MESSAGES_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
    }

    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Notificaciones activas")
            .setContentText("Escuchando notificaciones push")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return

        val foregroundChannel = NotificationChannel(
            FOREGROUND_CHANNEL_ID,
            "Servicio de notificaciones",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Mantiene la conexión de notificaciones en segundo plano"
        }

        val messagesChannel = NotificationChannel(
            MESSAGES_CHANNEL_ID,
            "Notificaciones push",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones recibidas desde el servidor"
        }

        manager.createNotificationChannel(foregroundChannel)
        manager.createNotificationChannel(messagesChannel)
    }

    companion object {
        const val EXTRA_EMAIL = "extra_email"

        private const val FOREGROUND_CHANNEL_ID = "ntfy_foreground"
        private const val MESSAGES_CHANNEL_ID = "ntfy_messages"
        private const val FOREGROUND_NOTIFICATION_ID = 1001
        private const val RECONNECT_DELAY_MS = 5000L

        fun start(context: Context, email: String?) {
            if (email.isNullOrBlank()) return
            val intent = Intent(context, NotificationService::class.java).apply {
                putExtra(EXTRA_EMAIL, email)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, NotificationService::class.java))
        }
    }
}