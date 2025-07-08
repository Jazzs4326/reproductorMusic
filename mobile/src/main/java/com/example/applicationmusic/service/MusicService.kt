package com.example.applicationmusic.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.applicationmusic.MainActivity
import com.example.applicationmusic.R
import com.google.android.gms.wearable.WearableListenerService
import com.google.android.gms.wearable.MessageEvent
import android.util.Log
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.tasks.Tasks

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
        player.addListener(object : androidx.media3.common.Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                notifyWearPlaybackState(isPlaying)
            }
        })
        
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

    private fun notifyWearPlaybackState(isPlaying: Boolean) {
        Thread {
            try {
                val nodes = Tasks.await(Wearable.getNodeClient(this).connectedNodes)
                for (node in nodes) {
                    val state = if (isPlaying) "playing" else "paused"
                    Wearable.getMessageClient(this).sendMessage(node.id, "/music_state", state.toByteArray())
                }
            } catch (e: Exception) {
                Log.e("MusicService", "Error enviando estado a Wear", e)
            }
        }.start()
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MusicService", "onStartCommand: action=${intent?.action}")
        when (intent?.action) {
            "ACTION_PLAY" -> {
                Log.d("MusicService", "Recibido PLAY")
                mediaSession?.player?.play()
                notifyWearPlaybackState(true)
            }
            "ACTION_PAUSE" -> {
                Log.d("MusicService", "Recibido PAUSE")
                mediaSession?.player?.pause()
                notifyWearPlaybackState(false)
            }
            "ACTION_NEXT" -> {
                Log.d("MusicService", "Recibido NEXT")
                mediaSession?.player?.seekToNextMediaItem()
            }
            "ACTION_PREVIOUS" -> {
                Log.d("MusicService", "Recibido PREVIOUS")
                mediaSession?.player?.seekToPreviousMediaItem()
            }
        }
        return super.onStartCommand(intent, flags, startId)
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

class MusicControlListenerService : WearableListenerService() {
    override fun onMessageReceived(event: MessageEvent) {
        Log.d("WearListener", "Mensaje recibido: path=${event.path}, data=${String(event.data)}")
        if (event.path == "/music_control") {
            val command = String(event.data)
            val ctx = applicationContext
            val intent = Intent(ctx, MusicService::class.java)
            when (command) {
                "play" -> intent.action = "ACTION_PLAY"
                "pause" -> intent.action = "ACTION_PAUSE"
                "next" -> intent.action = "ACTION_NEXT"
                "previous" -> intent.action = "ACTION_PREVIOUS"
            }
            Log.d("WearListener", "Enviando intent a MusicService: action=${intent.action}")
            ctx.startService(intent)
        }
    }
} 