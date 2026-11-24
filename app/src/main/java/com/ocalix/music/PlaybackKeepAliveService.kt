package com.ocalix.music

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class PlaybackKeepAliveService : Service() {
    private var title = "Ocalix"
    private var artist = "Music Player"
    private var paused = false

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL, "Ocalix playback", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Menjaga sesi pemutaran Ocalix tetap aktif"
                    setShowBadge(false)
                }
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> { stopForeground(STOP_FOREGROUND_REMOVE); stopSelf(); return START_NOT_STICKY }
            ACTION_PAUSE -> paused = true
            ACTION_START -> paused = false
        }
        title = intent?.getStringExtra("title") ?: title
        artist = intent?.getStringExtra("artist") ?: artist
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val open = PendingIntent.getActivity(this, 1, Intent(this, MainActivity::class.java), PendingFlags)
        val stop = PendingIntent.getService(this, 2, Intent(this, PlaybackKeepAliveService::class.java).setAction(ACTION_STOP), PendingFlags)
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title)
            .setContentText(artist + if (paused) " • Dijeda" else " • Sedang diputar")
            .setContentIntent(open)
            .setOngoing(!paused)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Tutup", stop)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.ocalix.action.START"
        const val ACTION_PAUSE = "com.ocalix.action.PAUSE"
        const val ACTION_STOP = "com.ocalix.action.STOP"
        private const val CHANNEL = "ocalix_playback"
        private const val NOTIFICATION_ID = 702
        private val PendingFlags get() = PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
    }
}
