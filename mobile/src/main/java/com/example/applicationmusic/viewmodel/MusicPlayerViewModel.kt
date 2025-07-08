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
    
    private var exoPlayer: ExoPlayer? = null
    
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
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }
                
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            _duration.value = duration
                        }
                        Player.STATE_ENDED -> {
                            playNext()
                        }
                    }
                }
            })
        }
        
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
    
    fun playSong(song: Song) {
        _currentSong.value = song
        _currentIndex.value = _playlist.value.indexOf(song)
        
        // Reproducción real con ExoPlayer
        val mediaItem = MediaItem.fromUri(song.audioUri)
        exoPlayer?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }
    
    fun playPause() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
    }
    
    fun playNext() {
        val currentIdx = _currentIndex.value
        val playlist = _playlist.value
        if (playlist.isNotEmpty()) {
            val nextIndex = (currentIdx + 1) % playlist.size
            playSong(playlist[nextIndex])
        }
    }
    
    fun playPrevious() {
        val currentIdx = _currentIndex.value
        val playlist = _playlist.value
        if (playlist.isNotEmpty()) {
            val prevIndex = if (currentIdx > 0) currentIdx - 1 else playlist.size - 1
            playSong(playlist[prevIndex])
        }
    }
    
    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }
    
    fun updatePosition() {
        viewModelScope.launch {
            exoPlayer?.let { player ->
                _currentPosition.value = player.currentPosition
            }
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
        
        // Si no hay canción actual, establecer la nueva como actual
        if (_currentSong.value == null) {
            _currentSong.value = song
            _currentIndex.value = _playlist.value.indexOf(song)
        }
    }
    
    fun removeSong(song: Song) {
        // Remover de la lista de archivos seleccionados
        val updatedSelectedFiles = _selectedFiles.value.filter { it.id != song.id }
        _selectedFiles.value = updatedSelectedFiles
        
        // Actualizar la playlist
        val deviceSongs = _playlist.value.filter { it.id.startsWith("temp_").not() }
        _playlist.value = deviceSongs + updatedSelectedFiles
        
        // Si la canción removida era la actual, cambiar a la siguiente
        if (_currentSong.value?.id == song.id) {
            if (_playlist.value.isNotEmpty()) {
                val newIndex = if (_currentIndex.value >= _playlist.value.size) 0 else _currentIndex.value
                _currentSong.value = _playlist.value[newIndex]
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
        exoPlayer?.release()
    }
} 