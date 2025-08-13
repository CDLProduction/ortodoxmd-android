package md.ortodox.ortodoxmd.ui.audiobook

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
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

    // Pasul 1: Procesăm structura ierarhică pe un thread de fundal
    private val audiobooksStructure: StateFlow<List<AudiobookCategory>> = repository.getAudiobooks()
        .map { audiobooks ->
            val groupedByCategory = audiobooks.groupBy {
                it.remoteUrlPath.trimStart('/').split("/").getOrNull(1) ?: "necunoscut"
            }
            groupedByCategory.map { (categoryKey, chaptersInCategory) ->
                val books = chaptersInCategory.groupBy {
                    val segments = it.remoteUrlPath.trimStart('/').split('/')
                    segments.getOrNull(segments.size - 2) ?: categoryKey
                }.map { (bookKey, chaptersInBook) ->
                    val firstChapterSegments = chaptersInBook.first().remoteUrlPath.trimStart('/').split('/')
                    val testamentKey = firstChapterSegments.getOrNull(firstChapterSegments.size - 3) ?: categoryKey
                    AudiobookBook(
                        name = bookKey.toDisplayableName(),
                        testament = testamentKey.toDisplayableName(),
                        chapters = chaptersInBook.sortedByChapterNumber()
                    )
                }
                val isSimpleCategory = books.size == 1 && books.first().name.equals(categoryKey.toDisplayableName(), ignoreCase = true)
                AudiobookCategory(
                    name = categoryKey.toDisplayableName(),
                    books = books.sortedBy { it.name },
                    isSimpleCategory = isSimpleCategory
                )
            }
        }
        .flowOn(Dispatchers.Default) // <--- OPTIMIZARE: Mutăm calculul de mai sus pe thread-ul Default
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pasul 2: Procesăm stările de descărcare pe un thread de fundal
    private val downloadInfo: StateFlow<Pair<Map<Long, WorkInfo.State>, Map<Long, Int>>> = repository.getDownloadWorkInfoFlow()
        .sample(200)
        .map { workInfos ->
            val states = workInfos.associate { extractAudiobookIdFromTags(it.tags) to it.state }.filterKeys { it != -1L }
            val progress = workInfos.associate { extractAudiobookIdFromTags(it.tags) to it.progress.getInt(AudioDownloadWorker.KEY_PROGRESS, 0) }.filterKeys { it != -1L }
            states to progress
        }
        .flowOn(Dispatchers.Default) // <--- OPTIMIZARE: Mutăm crearea map-urilor pe thread-ul Default
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(emptyMap(), emptyMap()))

    // Pasul 3: Combinăm totul, tot în fundal, pentru a livra starea finală către UI
    val uiState: StateFlow<AudiobooksUiState> = combine(
        audiobooksStructure,
        downloadInfo
    ) { structure, (states, progress) ->
        AudiobooksUiState(
            categories = structure,
            isLoading = structure.isEmpty(),
            downloadStates = states,
            downloadProgress = progress
        )
    }
        .flowOn(Dispatchers.Default) // <--- OPTIMIZARE: Combinarea finală se face tot în fundal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AudiobooksUiState(isLoading = true))


    val isDownloading: StateFlow<Boolean> = repository.getDownloadWorkInfoFlow()
        .map { workInfos -> workInfos.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _selectedBookName = MutableStateFlow<String?>(null)

    // Această parte era deja optimizată corect cu .flowOn(Dispatchers.Default)
    val selectedBookState: StateFlow<ChapterScreenState> = _selectedBookName
        .filterNotNull()
        .combine(uiState) { selectedName, state ->
            val book = state.categories.flatMap { it.books }.find { it.name == selectedName.toDisplayableName() }
            ChapterScreenState(book = book, isLoading = book == null)
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = ChapterScreenState(isLoading = true))

    fun selectBook(bookName: String) { _selectedBookName.value = bookName }
    fun clearBookSelection() { _selectedBookName.value = null }

    init {
        viewModelScope.launch { repository.syncAudiobooks() }
    }

    // ... restul funcțiilor rămân neschimbate ...
    private fun extractAudiobookIdFromTags(tags: Set<String>): Long {
        return tags.find { it.startsWith("audiobook_download_") }?.substringAfter("audiobook_download_")?.toLongOrNull() ?: -1L
    }

    fun downloadChapter(chapter: AudiobookEntity) {
        viewModelScope.launch { repository.startDownload(chapter) }
    }

    fun deleteChapter(chapter: AudiobookEntity) {
        viewModelScope.launch { repository.deleteChapter(chapter) }
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

    fun deleteAllDownloadedChapters(chapters: List<AudiobookEntity>) {
        viewModelScope.launch {
            val downloadedChapters = chapters.filter { it.isDownloaded }
            if (downloadedChapters.isNotEmpty()) {
                repository.deleteChapters(downloadedChapters)
            }
        }
    }

    fun cancelAllDownloads() {
        viewModelScope.launch { workManager.cancelAllWorkByTag(AudiobookRepository.DOWNLOAD_TAG) }
    }
}