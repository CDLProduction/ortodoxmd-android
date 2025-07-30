package md.ortodox.ortodoxmd.ui.audiobook

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity
import md.ortodox.ortodoxmd.data.repository.AudiobookRepository
import md.ortodox.ortodoxmd.data.worker.AudioDownloadWorker
import javax.inject.Inject

@HiltViewModel
class AudiobookViewModel @Inject constructor(
    private val repository: AudiobookRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val workManager = WorkManager.getInstance(context)

    val isDownloading: StateFlow<Boolean> = repository.getDownloadWorkInfoFlow()
        .map { workInfos -> workInfos.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val uiState: StateFlow<AudiobooksUiState> = combine(
        repository.getAudiobooks(),
        repository.getDownloadWorkInfoFlow()
    ) { audiobooks, workInfos ->
        val downloadStates = workInfos.associate { extractAudiobookIdFromTags(it.tags) to it.state }.filterKeys { it != -1L }
        val downloadProgress = workInfos.associate { extractAudiobookIdFromTags(it.tags) to it.progress.getInt(AudioDownloadWorker.KEY_PROGRESS, 0) }.filterKeys { it != -1L }
        val groupedByBook = audiobooks.groupBy { it.remoteUrlPath.split("/").getOrNull(3) ?: "carte_necunoscuta" }
        val books: List<AudiobookBook> = groupedByBook.map { (bookKey, chapters) ->
            val testamentKey = chapters.firstOrNull()?.remoteUrlPath?.split("/")?.getOrNull(2) ?: "testament_necunoscut"
            AudiobookBook(name = bookKey.toDisplayableName(), testament = testamentKey.toDisplayableName(), chapters = chapters.sortedBy { it.id })
        }
        AudiobooksUiState(
            categories = listOf(AudiobookCategory("Biblia", books.sortedBy { it.name })),
            isLoading = audiobooks.isEmpty() && workInfos.isEmpty(),
            downloadStates = downloadStates,
            downloadProgress = downloadProgress
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = AudiobooksUiState(isLoading = true))

    private val _selectedBookName = MutableStateFlow<String?>(null)
    val selectedBookState: StateFlow<ChapterScreenState> = _selectedBookName
        .filterNotNull()
        .combine(uiState) { selectedName, state ->
            val book = state.categories.flatMap { it.books }.find { it.name.fromDisplayableName() == selectedName }
            ChapterScreenState(book = book, isLoading = book == null)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = ChapterScreenState(isLoading = true))

    fun selectBook(bookName: String) { _selectedBookName.value = bookName.fromDisplayableName() }
    fun clearBookSelection() { _selectedBookName.value = null }

    init {
        viewModelScope.launch { repository.syncAudiobooks() }
    }

    private fun extractAudiobookIdFromTags(tags: Set<String>): Long {
        return tags.find { it.startsWith("audiobook_download_") }?.substringAfter("audiobook_download_")?.toLongOrNull() ?: -1L
    }

    fun downloadChapter(chapter: AudiobookEntity) {
        viewModelScope.launch { repository.startDownload(chapter) }
    }

    fun downloadAllChapters(chapters: List<AudiobookEntity>) {
        viewModelScope.launch {
            val currentStates = uiState.value.downloadStates
            val chaptersToDownload = chapters.filter {
                val state = currentStates[it.id]
                !it.isDownloaded && state != WorkInfo.State.SUCCEEDED && state != WorkInfo.State.ENQUEUED && state != WorkInfo.State.RUNNING
            }
            if (chaptersToDownload.isEmpty()) return@launch

            val maxConcurrentDownloads = 3
            val downloadQueues = List(maxConcurrentDownloads) { mutableListOf<AudiobookEntity>() }
            chaptersToDownload.forEachIndexed { index, chapter -> downloadQueues[index % maxConcurrentDownloads].add(chapter) }
            downloadQueues.forEach { queue ->
                if (queue.isNotEmpty()) {
                    var continuation = workManager.beginUniqueWork("chain_start_${queue.first().id}", ExistingWorkPolicy.REPLACE, repository.createDownloadWorkRequest(queue.first()))
                    for (i in 1 until queue.size) { continuation = continuation.then(repository.createDownloadWorkRequest(queue[i])) }
                    continuation.enqueue()
                }
            }
        }
    }

    fun cancelAllDownloads() {
        viewModelScope.launch { workManager.cancelAllWorkByTag(AudiobookRepository.DOWNLOAD_TAG) }
    }
}