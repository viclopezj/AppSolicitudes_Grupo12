package com.example.appsolicitudes_grupo12.model.aviso

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

// Muestra los avisos como notificaciones locales del teléfono.
class NotificadorAndroid(context: Context) : Notificador {

    private val appContext = context.applicationContext
    private val gestor = NotificationManagerCompat.from(appContext)

    init {
        // Crea el canal de notificaciones (si ya existe, no pasa nada)
        gestor.createNotificationChannel(
            NotificationChannelCompat.Builder(
                CANAL_ID,
                NotificationManagerCompat.IMPORTANCE_DEFAULT
            ).setName("Avisos de solicitudes").build()
        )
    }

    // Si no hay permiso o algo falla, no se muestra el aviso y la app sigue normal.
    @SuppressLint("MissingPermission")
    override fun publicar(aviso: AvisoSolicitud) {
        try {
            if (!tienePermiso()) return

            val notificacion = NotificationCompat.Builder(appContext, CANAL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(aviso.titulo)
                .setContentText(aviso.mensaje)
                .setStyle(NotificationCompat.BigTextStyle().bigText(aviso.mensaje))
                .setAutoCancel(true)
                .apply { intentAbrirApp()?.let { setContentIntent(it) } }
                .build()

            gestor.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notificacion)
        } catch (e: Exception) {
            Log.w("NotificadorAndroid", "No se pudo mostrar el aviso", e)
        }
    }

    // Desde Android 13 hay que tener concedido POST_NOTIFICATIONS.
    private fun tienePermiso(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Al tocar el aviso se abre la app.
    private fun intentAbrirApp(): PendingIntent? {
        val intent = appContext.packageManager
            .getLaunchIntentForPackage(appContext.packageName) ?: return null
        return PendingIntent.getActivity(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private companion object {
        const val CANAL_ID = "avisos_solicitudes"
    }
}