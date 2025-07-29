// Fișier complet: AudiobookPlayerViewModel.kt
package md.ortodox.ortodoxmd.ui.audiobook

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import android.util.Log
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity
import md.ortodox.ortodoxmd.data.network.NetworkModule
import md.ortodox.ortodoxmd.data.repository.AudiobookRepository
import md.ortodox.ortodoxmd.ui.playback.PlaybackService
import java.io.File
import javax.inject.Inject

data class PlayerUiState(
    val audiobook: AudiobookEntity? = null,
    val isPlaying: Boolean = false,
    val currentPositionMillis: Long = 0,
    val totalDurationMillis: Long = 0,
    val isReady: Boolean = false
)

@HiltViewModel
@UnstableApi // Adaugă adnotarea la nivel de clasă pentru a simplifica
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
            if (isPlaying) {
                startPlaybackPositionUpdates()
            } else {
                progressUpdateJob?.cancel()
            }
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
        observeAudiobookChanges()
    }

    private fun initializeController() {
        val chapterId = savedStateHandle.get<String>("chapterId")?.toLongOrNull()
        Log.d("AudiobookPlayerVM", "Initializing for chapter ID: $chapterId")

        if (chapterId == null) {
            Log.e("AudiobookPlayerVM", "Invalid chapterId from SavedStateHandle")
            return
        }

        viewModelScope.launch {
            val audiobook = repository.getById(chapterId)
            if (audiobook == null) {
                Log.e("AudiobookPlayerVM", "Audiobook not found for ID: $chapterId")
                return@launch
            }
            Log.d("AudiobookPlayerVM", "Loaded audiobook: ${audiobook.title}, isDownloaded: ${audiobook.isDownloaded}")
            _uiState.update { it.copy(audiobook = audiobook) }

            setupMediaController(audiobook)
        }
    }

    private fun getMediaUri(audiobook: AudiobookEntity): Uri? {
        return if (audiobook.isDownloaded && audiobook.localFilePath != null && File(audiobook.localFilePath!!).exists()) {
            Log.d("AudiobookPlayerVM", "Using local file: ${audiobook.localFilePath}")
            Uri.fromFile(File(audiobook.localFilePath!!))
        } else {
            // FIX: Folosește endpoint-ul corect pentru streaming de pe server
            val remoteUrl = "${NetworkModule.BASE_URL_AUDIOBOOKS}api/audiobooks/${audiobook.id}/stream"
            Log.d("AudiobookPlayerVM", "Using remote URL: $remoteUrl")
            Uri.parse(remoteUrl)
        }
    }

    private fun setupMediaController(audiobook: AudiobookEntity) {
        val sessionToken = SessionToken(application, ComponentName(application, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(application, sessionToken).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            mediaController?.addListener(playerListener)

            val mediaUri = getMediaUri(audiobook)
            if (mediaUri != null) {
                prepareAndPlay(mediaUri, audiobook)
            } else {
                Log.e("AudiobookPlayerVM", "Media URI is null, cannot play")
            }
        }, MoreExecutors.directExecutor())
    }

    private fun prepareAndPlay(uri: Uri, audiobook: AudiobookEntity) {
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaId(audiobook.id.toString())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(audiobook.title)
                    .setArtist(audiobook.author)
                    .build()
            )
            .build()

        mediaController?.setMediaItem(mediaItem, audiobook.lastPositionMillis)
        mediaController?.prepare()
        mediaController?.playWhenReady = true
    }

    private fun startPlaybackPositionUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch {
            while (true) {
                val currentPosition = mediaController?.currentPosition?.coerceAtLeast(0) ?: 0
                if (_uiState.value.currentPositionMillis != currentPosition) {
                    _uiState.update { it.copy(currentPositionMillis = currentPosition) }
                }
                delay(500)
            }
        }
    }

    fun onPlayPauseToggle() {
        mediaController?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }

    fun onSeek(position: Long) {
        mediaController?.seekTo(position)
        _uiState.update { it.copy(currentPositionMillis = position) }
    }

    fun onRewind() = mediaController?.seekBack()

    fun onForward() = mediaController?.seekForward()

    private fun saveCurrentProgress() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val currentPosition = mediaController?.currentPosition ?: currentState.currentPositionMillis
            currentState.audiobook?.let { audiobook ->
                if (currentPosition > 0 && currentPosition != audiobook.lastPositionMillis) {
                    repository.savePlaybackPosition(audiobook.id, currentPosition)
                    Log.d("AudiobookPlayerVM", "Saved position: $currentPosition for ID: ${audiobook.id}")
                }
            }
        }
    }

    private fun observeAudiobookChanges() {
        viewModelScope.launch {
            val chapterId = savedStateHandle.get<String>("chapterId")?.toLongOrNull() ?: return@launch
            repository.getByIdFlow(chapterId).collectLatest { audiobook ->
                audiobook?.let {
                    Log.d("AudiobookPlayerVM", "Audiobook updated: ${it.title}, isDownloaded: ${it.isDownloaded}")
                    _uiState.update { state -> state.copy(audiobook = it) }

                    // Verifică dacă URI-ul s-a schimbat (ex: de la remote la local după descărcare)
                    val newUri = getMediaUri(it)
                    val currentUri = mediaController?.currentMediaItem?.localConfiguration?.uri
                    if (newUri != null && newUri != currentUri) {
                        Log.d("AudiobookPlayerVM", "Media source changed. Reloading player.")
                        prepareAndPlay(newUri, it)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        saveCurrentProgress()
        progressUpdateJob?.cancel()
        mediaController?.removeListener(playerListener)
        MediaController.releaseFuture(controllerFuture)
        Log.d("AudiobookPlayerVM", "ViewModel cleared")
        super.onCleared()
    }
}