package com.example.applicationmusic.data

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long, // en milisegundos
    val albumArtUri: String? = null,
    val audioUri: String
) 