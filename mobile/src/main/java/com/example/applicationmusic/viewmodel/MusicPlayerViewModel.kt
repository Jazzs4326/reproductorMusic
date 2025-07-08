package com.example.applicationmusic.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import com.example.applicationmusic.data.Song
import com.example.applicationmusic.service.MusicService
import com.example.applicationmusic.utils.MusicLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicPlayerViewModel : ViewModel() {
    
    // Elimina ExoPlayer local
    // private var exoPlayer: ExoPlayer? = null
    private var mediaController: MediaController? = null
    
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _playlist = MutableStateFlow<List<Song>>(emptyList())
    val playlist: StateFlow<List<Song>> = _playlist.asStateFlow()
    
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()
    
    // Lista de archivos seleccionados por el usuario
    private val _selectedFiles = MutableStateFlow<List<Song>>(emptyList())
    val selectedFiles: StateFlow<List<Song>> = _selectedFiles.asStateFlow()
    
    fun initializePlayer(context: Context) {
        // Inicializar ExoPlayer
        // exoPlayer = ExoPlayer.Builder(context).build().apply {
        //     addListener(object : Player.Listener {
        //         override fun onIsPlayingChanged(isPlaying: Boolean) {
        //             _isPlaying.value = isPlaying
        //         }
                
        //         override fun onPlaybackStateChanged(playbackState: Int) {
        //             when (playbackState) {
        //                 Player.STATE_READY -> {
        //                     _duration.value = duration
        //                 }
        //                 Player.STATE_ENDED -> {
        //                     playNext()
        //                 }
        //             }
        //         }
        //     })
        // }
        
        // Intentar cargar música del dispositivo, si no hay, mostrar mensaje
        try {
            val deviceSongs = MusicLoader.loadMusicFromDevice(context)
            if (deviceSongs.isNotEmpty()) {
                _playlist.value = deviceSongs
                _currentSong.value = deviceSongs[0]
            } else {
                // Si no hay música en el dispositivo, mostrar mensaje
                _playlist.value = emptyList()
                _currentSong.value = null
            }
        } catch (e: Exception) {
            // Si hay error, mostrar mensaje
            _playlist.value = emptyList()
            _currentSong.value = null
        }
    }
    
    fun setMediaController(controller: MediaController) {
        mediaController = controller

        controller.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                _duration.value = controller.duration.coerceAtLeast(0L)
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = controller.currentMediaItemIndex
                val song = _playlist.value.getOrNull(index)
                if (song != null) {
                    _currentSong.value = song
                    _currentIndex.value = index
                }
            }
            override fun onPositionDiscontinuity(reason: Int) {
                _currentPosition.value = controller.currentPosition
            }
            override fun onEvents(player: Player, events: Player.Events) {
                _currentPosition.value = player.currentPosition
                _duration.value = player.duration.coerceAtLeast(0L)
            }
        })

        viewModelScope.launch {
            while (true) {
                _currentPosition.value = controller.currentPosition
                _duration.value = controller.duration.coerceAtLeast(0L)
                kotlinx.coroutines.delay(500)
            }
        }
    }

    private fun syncPlaylistWithController() {
        val items = _playlist.value.map { androidx.media3.common.MediaItem.fromUri(it.audioUri) }
        mediaController?.setMediaItems(items)
        mediaController?.prepare()
    }

    fun playSong(song: Song) {
        val index = _playlist.value.indexOf(song)
        if (index >= 0) {
            mediaController?.seekToDefaultPosition(index)
            mediaController?.play()
            _currentSong.value = song
            _currentIndex.value = index
        }
    }
    
    fun playPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) {
                controller.pause()
            } else {
                controller.play()
            }
        }
    }
    
    fun playNext() {
        mediaController?.seekToNextMediaItem()
    }
    
    fun playPrevious() {
        mediaController?.seekToPreviousMediaItem()
    }
    
    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }
    
    fun updatePosition() {
        viewModelScope.launch {
            // exoPlayer?.let { player ->
            //     _currentPosition.value = player.currentPosition
            // }
        }
    }
    
    fun addSelectedFile(song: Song) {
        // Verificar si la canción ya existe en la lista
        val existingSong = _selectedFiles.value.find { it.audioUri == song.audioUri }
        if (existingSong != null) {
            // La canción ya existe, no agregar duplicado
            return
        }
        
        val currentList = _selectedFiles.value.toMutableList()
        currentList.add(song)
        _selectedFiles.value = currentList
        
        // Actualizar la playlist combinando música del dispositivo y archivos seleccionados
        val deviceSongs = _playlist.value.filter { it.id.startsWith("temp_").not() }
        _playlist.value = deviceSongs + currentList
        syncPlaylistWithController()
        
        // Si no hay canción actual, establecer la nueva como actual
        if (_currentSong.value == null) {
            _currentSong.value = song
            _currentIndex.value = _playlist.value.indexOf(song)
        }
    }
    
    fun removeSong(song: Song) {
        // Remover de la playlist (sin importar el origen)
        val updatedPlaylist = _playlist.value.filter { it.id != song.id }
        _playlist.value = updatedPlaylist

        // Remover de la lista de seleccionados si aplica
        _selectedFiles.value = _selectedFiles.value.filter { it.id != song.id }
        syncPlaylistWithController()

        // Si la canción removida era la actual, cambiar a la siguiente
        if (_currentSong.value?.id == song.id) {
            if (updatedPlaylist.isNotEmpty()) {
                val newIndex = if (_currentIndex.value >= updatedPlaylist.size) 0 else _currentIndex.value
                _currentSong.value = updatedPlaylist[newIndex]
                _currentIndex.value = newIndex
            } else {
                _currentSong.value = null
                _currentIndex.value = 0
            }
        }
    }
    
    fun clearPlaylist() {
        _selectedFiles.value = emptyList()
        val deviceSongs = _playlist.value.filter { it.id.startsWith("temp_").not() }
        _playlist.value = deviceSongs
        syncPlaylistWithController()
        
        if (_playlist.value.isNotEmpty()) {
            _currentSong.value = _playlist.value[0]
            _currentIndex.value = 0
        } else {
            _currentSong.value = null
            _currentIndex.value = 0
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // exoPlayer?.release()
        mediaController?.release()
    }
} 