package com.example.applicationmusic.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.applicationmusic.MainActivity
import com.example.applicationmusic.R

@UnstableApi
class MusicService : MediaSessionService() {
    
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private lateinit var notificationManager: NotificationManager
    
    companion object {
        const val NOTIFICATION_ID = 200
        const val CHANNEL_ID = "music_player_channel"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar el reproductor
        player = ExoPlayer.Builder(this).build()
        
        // Crear el MediaSession
        mediaSession = MediaSession.Builder(this, player).build()
        
        // Configurar el notification manager
        notificationManager = getSystemService(NotificationManager::class.java)
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Canal para el reproductor de música"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
    
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
} 