package md.ortodox.ortodoxmd.ui

import android.app.Application
import android.content.ComponentName
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.repository.AudiobookRepository
import md.ortodox.ortodoxmd.ui.playback.PlaybackService
import javax.inject.Inject

data class MiniPlayerState(
    val isVisible: Boolean = false,
    val trackTitle: String = "",
    val trackAuthor: String = "",
    val isPlaying: Boolean = false,
    val currentTrackId: Long = -1L
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val application: Application,
    private val audiobookRepository: AudiobookRepository
) : ViewModel() {

    private val _miniPlayerState = MutableStateFlow(MiniPlayerState())
    val miniPlayerState = _miniPlayerState.asStateFlow()

    private var mediaController: MediaController? = null
    private lateinit var controllerFuture: ListenableFuture<MediaController>

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _miniPlayerState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            updateStateFromMediaItem(mediaItem)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            // --- AICI ESTE CORECTAREA ---
            // Starea corectă pentru un player oprit este STATE_IDLE.
            if (playbackState == Player.STATE_IDLE) {
                _miniPlayerState.update { it.copy(isVisible = false) }
            }
        }
    }

    init {
        initializeController()
    }

    @OptIn(UnstableApi::class)
    private fun initializeController() {
        val sessionToken = SessionToken(application, ComponentName(application, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(application, sessionToken).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            mediaController?.addListener(playerListener)
            // Sincronizează starea inițială
            updateStateFromMediaItem(mediaController?.currentMediaItem)
            _miniPlayerState.update { it.copy(isPlaying = mediaController?.isPlaying ?: false) }
        }, MoreExecutors.directExecutor())
    }

    private fun updateStateFromMediaItem(mediaItem: MediaItem?) {
        viewModelScope.launch {
            val trackId = mediaItem?.mediaId?.toLongOrNull() ?: -1L
            if (trackId != -1L) {
                // Verificăm dacă ID-ul corespunde unei cărți audio
                val audiobook = audiobookRepository.getById(trackId)
                if (audiobook != null) {
                    _miniPlayerState.update {
                        it.copy(
                            isVisible = true,
                            trackTitle = audiobook.title,
                            trackAuthor = audiobook.author,
                            currentTrackId = trackId
                        )
                    }
                } else {
                    _miniPlayerState.update { it.copy(isVisible = false) }
                }
            } else {
                _miniPlayerState.update { it.copy(isVisible = false) }
            }
        }
    }

    fun togglePlayPause() {
        mediaController?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    override fun onCleared() {
        mediaController?.removeListener(playerListener)
        MediaController.releaseFuture(controllerFuture)
        super.onCleared()
    }
}