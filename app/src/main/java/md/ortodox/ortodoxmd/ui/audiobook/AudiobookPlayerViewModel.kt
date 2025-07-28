package md.ortodox.ortodoxmd.ui.audiobook

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity
import md.ortodox.ortodoxmd.data.repository.AudiobookRepository
import md.ortodox.ortodoxmd.ui.playback.PlaybackService
import javax.inject.Inject

data class PlayerUiState(
    val audiobook: AudiobookEntity? = null,
    val isPlaying: Boolean = false,
    val currentPositionMillis: Long = 0,
    val totalDurationMillis: Long = 0,
    val isReady: Boolean = false
)

@HiltViewModel
class AudiobookPlayerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: AudiobookRepository,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState.asStateFlow()

    private var mediaController: MediaController? = null
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private var progressUpdateJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                _uiState.update {
                    it.copy(
                        isReady = true,
                        totalDurationMillis = mediaController?.duration?.coerceAtLeast(0) ?: 0
                    )
                }
            }
        }
    }

    init {
        initializeController()
        startPlaybackPositionUpdates()
    }

    @OptIn(UnstableApi::class)
    private fun initializeController() {
        val chapterId = savedStateHandle.get<String>("chapterId")?.toLongOrNull()
        Log.d("PlayerVM", "Attempting to load chapter with ID from route: $chapterId")

        if (chapterId == null) {
            Log.e("PlayerVM", "Error: chapterId is null or invalid from SavedStateHandle.")
            return
        }

        viewModelScope.launch {
            val book = repository.getById(chapterId)
            if (book == null) {
                Log.e("PlayerVM", "Error: AudiobookEntity not found in database for ID: $chapterId")
                return@launch
            }
            Log.d("PlayerVM", "Found chapter in DB: ${book.title}")
            _uiState.update { it.copy(audiobook = book) }

            val mediaUri: Uri? = if (book.isDownloaded && book.localFilePath != null) {
                Log.d("PlayerVM", "Playing from local file: ${book.localFilePath}")
                book.localFilePath!!.toUri()
            } else {
                val url = "http://127.0.0.1:8081/api/audiobooks/${book.id}/stream"
                Log.d("PlayerVM", "Playing from remote URL: $url")
                url.toUri()
            }

            val sessionToken = SessionToken(application, ComponentName(application, PlaybackService::class.java))
            controllerFuture = MediaController.Builder(application, sessionToken).buildAsync()
            controllerFuture.addListener({
                mediaController = controllerFuture.get()
                mediaController?.addListener(playerListener)
                Log.d("PlayerVM", "MediaController connected. Preparing to play.")

                if (mediaUri != null) {
                    prepareAndPlay(mediaUri, book)
                } else {
                    Log.e("PlayerVM", "Cannot start playback, mediaUri is null.")
                }

            }, MoreExecutors.directExecutor())
        }
    }

    private fun prepareAndPlay(uri: Uri, book: AudiobookEntity) {
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(book.title)
                    .setArtist(book.author)
                    .build()
            )
            .build()

        mediaController?.setMediaItem(mediaItem)
        mediaController?.prepare()
        mediaController?.seekTo(book.lastPositionMillis)
        mediaController?.play()
    }

    private fun startPlaybackPositionUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch {
            while (true) {
                val controller = mediaController
                if (controller?.isPlaying == true) {
                    _uiState.update {
                        it.copy(currentPositionMillis = controller.currentPosition.coerceAtLeast(0))
                    }
                }
                delay(500)
            }
        }
    }

    fun onPlayPauseToggle() {
        if (mediaController?.isPlaying == true) {
            mediaController?.pause()
            saveCurrentProgress() // Salvează la pauză
        } else {
            mediaController?.play()
        }
    }

    fun onSeek(position: Long) {
        mediaController?.seekTo(position)
        _uiState.update { it.copy(currentPositionMillis = position) }
        saveCurrentProgress() // Salvează după seek
    }

    fun onRewind() {
        mediaController?.seekBack()
        saveCurrentProgress() // Salvează după rewind
    }

    fun onForward() {
        mediaController?.seekForward()
        saveCurrentProgress() // Salvează după forward
    }

    private fun saveCurrentProgress() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val currentPosition = mediaController?.currentPosition ?: currentState.currentPositionMillis
            if (currentState.audiobook != null && currentPosition > 0) {
                repository.savePlaybackPosition(currentState.audiobook.id, currentPosition)
                Log.d("PlayerVM", "Saved playback position: $currentPosition for book ID: ${currentState.audiobook.id}")
            }
        }
    }

    override fun onCleared() {
        saveCurrentProgress() // Salvează progresul la ieșirea din ecran
        progressUpdateJob?.cancel()
        mediaController?.removeListener(playerListener)
        MediaController.releaseFuture(controllerFuture)
        Log.d("PlayerVM", "ViewModel cleared and resources released.")
        super.onCleared()
    }
}
