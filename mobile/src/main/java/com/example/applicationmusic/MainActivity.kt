package com.example.applicationmusic

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.applicationmusic.ui.components.PlayerControls
import com.example.applicationmusic.ui.components.Playlist
import com.example.applicationmusic.ui.theme.ApplicationMusicTheme
import com.example.applicationmusic.viewmodel.MusicPlayerViewModel
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.applicationmusic.service.MusicService
import android.content.ComponentName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            enableEdgeToEdge()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            setContent {
                ApplicationMusicTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainScreen()
                    }
                }
            }
        } catch (e: Exception) {
            // Log del error para debugging
            android.util.Log.e("MainActivity", "Error en onCreate: ${e.message}", e)
            
            // Fallback simple si hay error
            setContent {
                ApplicationMusicTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Reproductor de Música",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Cargando...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: MusicPlayerViewModel = viewModel()
) {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        try {
            viewModel.initializePlayer(context)
            // Crear y conectar MediaController de forma asíncrona
            val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
            val controller = withContext(Dispatchers.IO) {
                MediaController.Builder(context, sessionToken).buildAsync().get()
            }
            viewModel.setMediaController(controller)
        } catch (e: Exception) {
            android.util.Log.e("MainScreen", "Error inicializando player/controller: ${e.message}", e)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Reproductor de Música",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Controles del reproductor
        PlayerControls(viewModel = viewModel)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Lista de canciones
        Text(
            text = "Lista de Reproducción",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Lista de canciones con scroll propio
        Playlist(viewModel = viewModel)
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ApplicationMusicTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Reproductor de Música",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "Vista previa del reproductor",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}