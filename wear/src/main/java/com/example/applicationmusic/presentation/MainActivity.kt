/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.applicationmusic.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import androidx.wear.compose.material.Button
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.applicationmusic.R
import com.example.applicationmusic.presentation.theme.ApplicationMusicTheme
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import android.widget.Toast

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    ApplicationMusicTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            MusicRemoteControls()
        }
    }
}

@Composable
fun MusicRemoteControls() {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }

    // Listener para recibir el estado real desde el móvil
    DisposableEffect(Unit) {
        val messageListener = object : MessageClient.OnMessageReceivedListener {
            override fun onMessageReceived(event: MessageEvent) {
                if (event.path == "/music_state") {
                    val state = String(event.data)
                    isPlaying = state == "playing"
                }
            }
        }
        val client = Wearable.getMessageClient(context)
        client.addListener(messageListener)
        onDispose { client.removeListener(messageListener) }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Control Remoto",
            style = MaterialTheme.typography.title2,
            modifier = Modifier.padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { sendWearCommand(context, "previous") }) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = "Anterior")
            }
            Button(onClick = {
                val cmd = if (isPlaying) "pause" else "play"
                sendWearCommand(context, cmd)
                // isPlaying = !isPlaying // Ya no alternar localmente, esperar confirmación del móvil
            }) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pausar" else "Reproducir"
                )
            }
            Button(onClick = { sendWearCommand(context, "next") }) {
                Icon(Icons.Filled.SkipNext, contentDescription = "Siguiente")
            }
        }
    }
}

fun sendWearCommand(context: android.content.Context, command: String) {
    Log.d("WearRemote", "Enviando comando: $command")
    Toast.makeText(context, "Enviando: $command", Toast.LENGTH_SHORT).show()
    val messageClient = Wearable.getMessageClient(context)
    Thread {
        try {
            val nodes = Tasks.await(Wearable.getNodeClient(context).connectedNodes)
            for (node in nodes) {
                Log.d("WearRemote", "Enviando a nodo: ${node.displayName}")
                messageClient.sendMessage(node.id, "/music_control", command.toByteArray())
                    .addOnSuccessListener { Log.d("WearRemote", "Mensaje enviado a ${node.displayName}") }
                    .addOnFailureListener { Log.e("WearRemote", "Error enviando mensaje", it) }
            }
        } catch (e: Exception) {
            Log.e("WearRemote", "Error en sendWearCommand", e)
        }
    }.start()
}

@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp()
}