package md.ortodox.ortodoxmd.ui.radio

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata // Asigură-te că ai acest import
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import md.ortodox.ortodoxmd.ui.playback.PlaybackService // MODIFICARE: Importăm noul serviciu
import javax.inject.Inject

data class RadioUiState(
    val stations: List<RadioStation> = emptyList(),
    val currentStation: RadioStation? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false
)

@HiltViewModel
class RadioViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(RadioUiState())
    val uiState = _uiState.asStateFlow()

    private var mediaController: MediaController? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val isBuffering = playbackState == Player.STATE_BUFFERING
            _uiState.update { it.copy(isBuffering = isBuffering) }
        }
    }

    init {
        val radioStations = listOf(
            RadioStation("Radio Logos Moldova", "https://www.radio.md/stream/radiologos"),
            RadioStation("Ancient Faith Radio", "https://ancientfaith.streamguys1.com/music"),
            RadioStation("Radio Doxologia", "https://rlive.doxologia.ro/stream.mp3")
        )
        _uiState.update { it.copy(stations = radioStations) }

        // MODIFICARE 1: Ne conectăm la noul PlaybackService în loc de vechiul RadioService
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            mediaController?.addListener(playerListener)
        }, MoreExecutors.directExecutor())
    }

    fun onStationSelected(station: RadioStation) {
        _uiState.update { it.copy(currentStation = station) }
        mediaController?.let {
            val mediaItem = MediaItem.Builder()
                .setUri(station.streamUrl)
                .setMediaId(station.name)
                // MODIFICARE 2: Adăugăm metadate pentru ca notificarea să arate corect
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(station.name)
                        .setArtist("Radio Live")
                        .build()
                )
                .build()
            it.setMediaItem(mediaItem)
            it.prepare()
            it.play()
        }
    }

    fun togglePlayback() {
        mediaController?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    override fun onCleared() {
        mediaController?.removeListener(playerListener)
        // MediaController este eliberat de sistem, dar e o practică bună să nu lăsăm referințe
        mediaController = null
        super.onCleared()
    }
}