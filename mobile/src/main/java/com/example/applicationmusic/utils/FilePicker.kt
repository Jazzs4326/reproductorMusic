package com.example.applicationmusic.utils

import android.content.Context
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.example.applicationmusic.data.Song

object FilePicker {
    
    fun createAudioFilePicker(
        activity: FragmentActivity,
        onFileSelected: (Song) -> Unit
    ) = activity.registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            // Crear una canción temporal con el archivo seleccionado
            val song = Song(
                id = "temp_${System.currentTimeMillis()}",
                title = getFileNameFromUri(activity, selectedUri),
                artist = "Archivo Seleccionado",
                album = "Archivo Local",
                duration = 0L, // La duración se obtendrá cuando se reproduzca
                audioUri = selectedUri.toString()
            )
            onFileSelected(song)
        }
    }
    
    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex("_display_name")
                if (nameIndex != -1 && it.moveToFirst()) {
                    it.getString(nameIndex) ?: "Archivo de Audio"
                } else {
                    "Archivo de Audio"
                }
            } ?: "Archivo de Audio"
        } catch (e: Exception) {
            "Archivo de Audio"
        }
    }
} 