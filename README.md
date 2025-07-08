# Reproductor de Música Android

Una aplicación de reproductor de música desarrollada con Jetpack Compose y Media3 para Android.

## Características

- 🎵 Reproducción de música con controles básicos (play/pause, siguiente, anterior)
- 📱 Interfaz moderna con Material Design 3
- 📋 Lista de reproducción con canciones de ejemplo
- ⏱️ Barra de progreso con control de tiempo
- 🎨 Diseño responsive y accesible

## Tecnologías Utilizadas

- **Jetpack Compose**: UI moderna declarativa
- **Media3**: Biblioteca de reproducción multimedia de Android
- **ViewModel**: Gestión del estado de la aplicación
- **Kotlin Coroutines**: Programación asíncrona
- **Material Design 3**: Sistema de diseño moderno

## Estructura del Proyecto

```
mobile/src/main/java/com/example/applicationmusic/
├── data/
│   └── Song.kt                    # Modelo de datos para canciones
├── service/
│   └── MusicService.kt            # Servicio de reproducción en segundo plano
├── ui/
│   └── components/
│       └── MusicPlayerComponents.kt # Componentes de UI del reproductor
├── viewmodel/
│   └── MusicPlayerViewModel.kt    # Lógica de negocio del reproductor
└── MainActivity.kt                # Actividad principal
```

## Funcionalidades

### Controles de Reproducción
- **Play/Pause**: Reproducir o pausar la canción actual
- **Siguiente**: Ir a la siguiente canción en la lista
- **Anterior**: Ir a la canción anterior en la lista
- **Barra de Progreso**: Control de tiempo de reproducción

### Lista de Reproducción
- Muestra todas las canciones disponibles
- Indica la canción que se está reproduciendo actualmente
- Permite seleccionar cualquier canción para reproducir

## Configuración

### Permisos Requeridos
La aplicación requiere los siguientes permisos:
- `INTERNET`: Para reproducir música desde URLs
- `WAKE_LOCK`: Para mantener la reproducción activa
- `FOREGROUND_SERVICE`: Para reproducción en segundo plano
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK`: Para el servicio de música

### Dependencias
Las principales dependencias incluyen:
- `androidx.media3:media3-exoplayer`: Motor de reproducción
- `androidx.media3:media3-ui`: Componentes de UI para Media3
- `androidx.media3:media3-session`: Gestión de sesiones de media
- `androidx.lifecycle:lifecycle-viewmodel-compose`: Integración de ViewModel con Compose

## Uso

1. **Compilar y Ejecutar**: Abre el proyecto en Android Studio y ejecuta la aplicación
2. **Seleccionar Canción**: Toca cualquier canción en la lista para comenzar la reproducción
3. **Controles**: Usa los botones de control para navegar entre canciones
4. **Progreso**: Desliza en la barra de progreso para cambiar la posición de reproducción

## Personalización

### Agregar Nuevas Canciones
Para agregar nuevas canciones, modifica el método `loadSampleSongs()` en `MusicPlayerViewModel.kt`:

```kotlin
private fun loadSampleSongs() {
    val sampleSongs = listOf(
        Song(
            id = "nuevo_id",
            title = "Nueva Canción",
            artist = "Nuevo Artista",
            album = "Nuevo Álbum",
            duration = 180000, // 3 minutos en milisegundos
            audioUri = "https://ejemplo.com/cancion.mp3"
        )
        // ... más canciones
    )
    _playlist.value = sampleSongs
}
```

### Modificar la UI
Los componentes de UI están en `MusicPlayerComponents.kt` y pueden ser personalizados según tus necesidades.

## Próximas Mejoras

- [ ] Reproducción de archivos locales del dispositivo
- [ ] Integración con biblioteca de música del dispositivo
- [ ] Ecualizador y efectos de audio
- [ ] Modo aleatorio y repetición
- [ ] Favoritos y playlists personalizadas
- [ ] Búsqueda de canciones
- [ ] Sincronización con servicios de música en la nube

## Contribución

Para contribuir al proyecto:
1. Fork el repositorio
2. Crea una rama para tu feature
3. Realiza tus cambios
4. Envía un pull request

## Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo LICENSE para más detalles. 