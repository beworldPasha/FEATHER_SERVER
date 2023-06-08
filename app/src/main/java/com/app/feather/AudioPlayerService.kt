package com.app.feather

import PlayerManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.SimpleExoPlayer

class AudioPlayerService : Service() {

    private var mediaPlayer: SimpleExoPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "AudioPlayerChannel"
        private const val NOTIFICATION_ID = 1
        private var instance: AudioPlayerService? = null

        fun getInstance(): AudioPlayerService? {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        mediaPlayer = PlayerManager.getInstance()?.player

        // Создание и инициализация MediaSessionCompat
        mediaSession = MediaSessionCompat(this, "AudioPlayerService")
        mediaSession.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )

        val audioPlayerBroadcastReceiver = AudioPlayerBroadcastReceiver()

        val playPauseFilter = IntentFilter("ACTION_PLAY_PAUSE")
        val nextFilter = IntentFilter("ACTION_NEXT")
        val previousFilter = IntentFilter("ACTION_PREVIOUS")
        val dismissPendingFilter = IntentFilter("com.app.feather.NOTIFICATION_DISMISSED")

        registerReceiver(audioPlayerBroadcastReceiver, playPauseFilter)
        registerReceiver(audioPlayerBroadcastReceiver, nextFilter)
        registerReceiver(audioPlayerBroadcastReceiver, previousFilter)
        registerReceiver(audioPlayerBroadcastReceiver, dismissPendingFilter)

        // Создание уведомления с MediaStyle
        createNotificationChannel()
        val notification = buildNotification(R.drawable.pause_icon)
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        PlayerManager.getInstance()?.player?.pause()
        mediaSession.release()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        val name = "Audio Player"
        val descriptionText = "Audio Player Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
        channel.description = descriptionText
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    fun buildNotification(toggleIcon: Int): Notification {
        val channelId = NOTIFICATION_CHANNEL_ID
        val contentIntent =
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )

        // Создание кнопок управления для уведомления
        val playPauseIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent("ACTION_PLAY_PAUSE"),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val nextTrackIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent("ACTION_NEXT"),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val prevTrackIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent("ACTION_PREVIOUS"),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val dismissIntent = Intent("com.app.feather.NOTIFICATION_DISMISSED")
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            dismissIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Создание MediaStyle для уведомления
        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(0, 1, 2) // Порядок кнопок управления

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Audio Player")
            .setContentText("Now playing: Song Title")
            .setSmallIcon(R.drawable.feather_logo32)
            //.setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.previous_icon, "Previous", prevTrackIntent)
            .addAction(toggleIcon, "Play/Pause", playPauseIntent)
            .addAction(R.drawable.previous_icon, "Next", nextTrackIntent)
            .setDeleteIntent(dismissPendingIntent)
            .setStyle(mediaStyle)
            .setShowWhen(false)
        notificationBuilder.setOngoing(false)
        return notificationBuilder.build()
    }

    fun updateService(context: Context?, notification: Notification) {
        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    inner class AudioPlayerBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Обработка нажатия кнопок управления
            when (intent?.action) {
                "ACTION_PLAY_PAUSE" -> {
                    PlayerManager.getInstance()?.togglePlayback()

                    val notification = if (PlayerManager.getInstance()?.player?.isPlaying == true) {
                        buildNotification(R.drawable.pause_icon)
                    } else {
                        stopForeground(true)
                        buildNotification(R.drawable.play_icon)
                    }
                    updateService(context, notification)

                }

                "com.app.feather.NOTIFICATION_DISMISSED" -> {
                    PlayerManager.getInstance()?.pause()
                    PlayerManager.getInstance()?.updatePlaybackIcon()
                }

                "ACTION_NEXT" -> {
                    // Логика для кнопки Next
                }

                "ACTION_PREVIOUS" -> {
                    // Логика для кнопки Previous
                }
            }
        }
    }
}