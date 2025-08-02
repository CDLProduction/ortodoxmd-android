package md.ortodox.ortodoxmd.ui.audiobook

import android.app.Application
import android.content.ComponentName
import android.net.Uri
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
import kotlinx.coroutines.flow.first
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
    val chapters: List<AudiobookEntity> = emptyList(),
    val currentChapterIndex: Int = 0,
    val isPlaying: Boolean = false,
    val currentPositionMillis: Long = 0,
    val totalDurationMillis: Long = 0,
    val isReady: Boolean = false
)

@HiltViewModel
@UnstableApi
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
            if (playbackState == Player.STATE_ENDED) {
                onNext()
            }
        }
    }

    init {
        initializeController()
    }

    private fun initializeController() {
        // **CORECTAT: Citim argumentul direct ca Long**
        val chapterId: Long? = savedStateHandle["chapterId"]
        if (chapterId == null) return

        viewModelScope.launch {
            val audiobook = repository.getById(chapterId) ?: return@launch
            // Găsește toate capitolele din aceeași carte
            val bookPathPrefix = audiobook.remoteUrlPath.split("/").take(4).joinToString("/")

            // **CORECTAT: Folosim funcția de sortare corectă**
            val allChapters = repository.getAudiobooks().first()
                .filter { it.remoteUrlPath.startsWith(bookPathPrefix) }
                .sortedByChapterNumber()

            val currentIndex = allChapters.indexOfFirst { it.id == chapterId }

            _uiState.update { it.copy(audiobook = audiobook, chapters = allChapters, currentChapterIndex = currentIndex) }
            setupMediaController(audiobook)
        }
    }

    private fun getMediaUri(audiobook: AudiobookEntity): Uri? {
        return if (audiobook.isDownloaded && audiobook.localFilePath != null && File(audiobook.localFilePath!!).exists()) {
            Uri.fromFile(File(audiobook.localFilePath!!))
        } else {
            Uri.parse("${NetworkModule.BASE_URL_AUDIOBOOKS}api/audiobooks/${audiobook.id}/stream")
        }
    }

    private fun setupMediaController(audiobook: AudiobookEntity) {
        val sessionToken = SessionToken(application, ComponentName(application, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(application, sessionToken).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            mediaController?.addListener(playerListener)
            getMediaUri(audiobook)?.let { prepareAndPlay(it, audiobook, audiobook.lastPositionMillis) }
        }, MoreExecutors.directExecutor())
    }

    private fun prepareAndPlay(uri: Uri, audiobook: AudiobookEntity, startPosition: Long = 0) {
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
        mediaController?.setMediaItem(mediaItem, startPosition)
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
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun onSeek(position: Long) {
        mediaController?.seekTo(position)
        _uiState.update { it.copy(currentPositionMillis = position) }
    }

    fun onRewind() = mediaController?.seekBack()
    fun onForward() = mediaController?.seekForward()

    fun onNext() {
        val currentState = _uiState.value
        val nextIndex = currentState.currentChapterIndex + 1
        if (nextIndex < currentState.chapters.size) {
            val nextChapter = currentState.chapters[nextIndex]
            _uiState.update { it.copy(audiobook = nextChapter, currentChapterIndex = nextIndex) }
            getMediaUri(nextChapter)?.let { prepareAndPlay(it, nextChapter) }
        } else {
            mediaController?.pause() // Oprește la finalul listei
        }
    }

    fun onPrevious() {
        val currentState = _uiState.value
        val prevIndex = currentState.currentChapterIndex - 1
        if (prevIndex >= 0) {
            val prevChapter = currentState.chapters[prevIndex]
            _uiState.update { it.copy(audiobook = prevChapter, currentChapterIndex = prevIndex) }
            getMediaUri(prevChapter)?.let { prepareAndPlay(it, prevChapter) }
        }
    }

    override fun onCleared() {
        progressUpdateJob?.cancel()
        mediaController?.removeListener(playerListener)
        MediaController.releaseFuture(controllerFuture)
        super.onCleared()
    }
}