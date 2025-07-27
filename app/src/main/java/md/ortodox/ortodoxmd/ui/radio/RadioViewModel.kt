package md.ortodox.ortodoxmd.ui.radio

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Starea UI pentru ecranul Radio.
 * Am adăugat o stare nouă, 'isBuffering', pentru a oferi feedback utilizatorului.
 */
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

    // NOU: Listener pentru a primi actualizări de la player
    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            _uiState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            val isBuffering = playbackState == Player.STATE_BUFFERING
            _uiState.update { it.copy(isBuffering = isBuffering) }
        }
    }

    init {
        // *** CORECȚIE APLICATĂ AICI: Am actualizat lista de radiouri ***
        val radioStations = listOf(
            RadioStation("Radio Logos Moldova", "https://www.radio.md/stream/radiologos"),
            RadioStation("Ancient Faith Radio", "https://ancientfaith.streamguys1.com/music"),
            RadioStation("Radio Doxologia", "https://rlive.doxologia.ro/stream.mp3")
        )
        _uiState.update { it.copy(stations = radioStations) }

        // Conectarea la serviciul media
        val sessionToken = SessionToken(context, ComponentName(context, RadioService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                mediaController = controllerFuture.get()
                // NOU: Adăugăm listener-ul la controller odată ce este gata
                mediaController?.addListener(playerListener)
            },
            MoreExecutors.directExecutor()
        )
    }

    fun onStationSelected(station: RadioStation) {
        _uiState.update { it.copy(currentStation = station) }
        mediaController?.let {
            val mediaItem = MediaItem.Builder()
                .setUri(station.streamUrl)
                .setMediaId(station.name) // Setăm un ID pentru notificare
                .build()
            it.setMediaItem(mediaItem)
            it.prepare()
            it.play()
            // Nu mai actualizăm 'isPlaying' manual aici; listener-ul o va face.
        }
    }

    fun togglePlayback() {
        mediaController?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
            // Nu mai actualizăm 'isPlaying' manual aici; listener-ul o va face.
        }
    }

    override fun onCleared() {
        // NOU: Eliminăm listener-ul pentru a preveni memory leaks
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        super.onCleared()
    }
}
