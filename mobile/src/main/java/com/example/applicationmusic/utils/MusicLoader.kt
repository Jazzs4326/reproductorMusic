package com.example.applicationmusic.utils

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.example.applicationmusic.data.Song

object MusicLoader {
    
    fun loadMusicFromDevice(context: Context): List<Song> {
        val songs = mutableListOf<Song>()
        val contentResolver: ContentResolver = context.contentResolver
        
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        
        val cursor: Cursor? = contentResolver.query(
            uri,
            null,
            selection,
            null,
            sortOrder
        )
        
        cursor?.use { c ->
            val idColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            
            while (c.moveToNext()) {
                val id = c.getLong(idColumn)
                val title = c.getString(titleColumn) ?: "Título Desconocido"
                val artist = c.getString(artistColumn) ?: "Artista Desconocido"
                val album = c.getString(albumColumn) ?: "Álbum Desconocido"
                val duration = c.getLong(durationColumn)
                
                val contentUri = Uri.withAppendedPath(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )
                
                songs.add(
                    Song(
                        id = id.toString(),
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        audioUri = contentUri.toString()
                    )
                )
            }
        }
        
        return songs
    }
    
    fun loadSampleSongs(): List<Song> {
        return listOf(
            Song(
                id = "1",
                title = "Música Libre - Jazz",
                artist = "Artista Libre",
                album = "Música de Ejemplo",
                duration = 180000, // 3 minutos
                audioUri = "https://www.soundjay.com/misc/sounds/bell-ringing-05.wav"
            ),
            Song(
                id = "2",
                title = "Música Libre - Piano",
                artist = "Artista Libre",
                album = "Música de Ejemplo",
                duration = 240000, // 4 minutos
                audioUri = "https://www.soundjay.com/misc/sounds/bell-ringing-05.wav"
            ),
            Song(
                id = "3",
                title = "Música Libre - Ambient",
                artist = "Artista Libre",
                album = "Música de Ejemplo",
                duration = 200000, // 3:20 minutos
                audioUri = "https://www.soundjay.com/misc/sounds/bell-ringing-05.wav"
            )
        )
    }
} 