package md.ortodox.ortodoxmd.ui.audiobook

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
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

    // --- FLUXURI DE BAZĂ (PRIVATE) ---

    private val audiobooksStructure: StateFlow<ImmutableList<AudiobookCategory>> = repository.getAudiobooks()
        .map { audiobooks -> transformToGroupedCategories(audiobooks) }
        .flowOn(Dispatchers.Default) // Optimizare 1
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(10000), persistentListOf()) // Optimizare 2

    private val downloadInfo: StateFlow<Pair<ImmutableMap<Long, WorkInfo.State>, ImmutableMap<Long, Int>>> = repository.getDownloadWorkInfoFlow()
        .sample(300) // Am mărit intervalul pentru a reduce și mai mult lag-ul
        .map { workInfos ->
            val states = workInfos.associate { extractAudiobookIdFromTags(it.tags) to it.state }.filterKeys { it != -1L }
            val progress = workInfos.associate { extractAudiobookIdFromTags(it.tags) to it.progress.getInt(AudioDownloadWorker.KEY_PROGRESS, 0) }.filterKeys { it != -1L }
            states.toImmutableMap() to progress.toImmutableMap() // Optimizare 2
        }
        .flowOn(Dispatchers.Default) // Optimizare 1
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(persistentMapOf(), persistentMapOf())) // Optimizare 2

    // --- STĂRI PUBLICE PENTRU UI ---

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
        .flowOn(Dispatchers.Default) // Optimizare 1
        .distinctUntilChanged() // Optimizare 3
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AudiobooksUiState(isLoading = true))

    val downloadedUiState: StateFlow<DownloadedAudiobooksUiState> = repository.getDownloadedAudiobooks()
        .map { downloadedAudiobooks -> transformToGroupedCategories(downloadedAudiobooks) }
        .map { groupedCategories ->
            DownloadedAudiobooksUiState(
                categories = groupedCategories,
                isLoading = false
            )
        }
        .flowOn(Dispatchers.Default) // Optimizare 1
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DownloadedAudiobooksUiState(isLoading = true)
        )

    val isDownloading: StateFlow<Boolean> = repository.getDownloadWorkInfoFlow()
        .map { workInfos -> workInfos.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _selectedBookName = MutableStateFlow<String?>(null)
    val selectedBookState: StateFlow<ChapterScreenState> = _selectedBookName
        .filterNotNull()
        .combine(uiState) { selectedName, state ->
            val book = state.categories.flatMap { it.books }.find { it.name == selectedName.toDisplayableName() }
            ChapterScreenState(book = book, isLoading = book == null)
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = ChapterScreenState(isLoading = true))

    // --- ACȚIUNI ---

    init {
        viewModelScope.launch { repository.syncAudiobooks() }
    }

    fun selectBook(bookName: String) { _selectedBookName.value = bookName }
    fun clearBookSelection() { _selectedBookName.value = null }

    fun downloadChapter(chapter: AudiobookEntity) = viewModelScope.launch { repository.startDownload(chapter) }
    fun deleteChapter(chapter: AudiobookEntity) = viewModelScope.launch { repository.deleteChapter(chapter) }
    fun cancelAllDownloads() = viewModelScope.launch { workManager.cancelAllWorkByTag(AudiobookRepository.DOWNLOAD_TAG) }
    fun deleteAllDownloadedChapters(chapters: List<AudiobookEntity>) = viewModelScope.launch {
        repository.deleteChapters(chapters.filter { it.isDownloaded })
    }

    fun downloadAllChapters(chapters: List<AudiobookEntity>) {
        // Mutăm și logica de planificare pe un thread de I/O
        viewModelScope.launch(Dispatchers.IO) {
            val currentDownloadStates = uiState.value.downloadStates
            // Filtrăm doar capitolele care nu sunt deja descărcate sau în coadă/progres
            val chaptersToDownload = chapters.filter {
                val state = currentDownloadStates[it.id]
                !it.isDownloaded && state != WorkInfo.State.SUCCEEDED && state != WorkInfo.State.ENQUEUED && state != WorkInfo.State.RUNNING
            }
            if (chaptersToDownload.isEmpty()) return@launch

            // Creăm o listă de cereri de muncă (WorkRequest) pentru fiecare capitol
            val workRequests = chaptersToDownload.map { chapter ->
                repository.createDownloadWorkRequest(chapter)
            }

            // Construirea și pornirea lanțului secvențial
            var continuation = workManager.beginUniqueWork(
                "download_all_chapters_chain",
                ExistingWorkPolicy.REPLACE,
                workRequests.first()
            )

            for (i in 1 until workRequests.size) {
                continuation = continuation.then(workRequests[i])
            }

            continuation.enqueue()
        }
    }


    // --- FUNCȚII PRIVATE AJUTĂTOARE ---

    private fun extractAudiobookIdFromTags(tags: Set<String>): Long {
        return tags.find { it.startsWith("audiobook_download_") }?.substringAfter("audiobook_download_")?.toLongOrNull() ?: -1L
    }

    private fun transformToGroupedCategories(audiobooks: List<AudiobookEntity>): ImmutableList<AudiobookCategory> {
        if (audiobooks.isEmpty()) return persistentListOf()
        val groupedByCategory = audiobooks.groupBy { it.remoteUrlPath.trimStart('/').split("/").getOrNull(1) ?: "necunoscut" }
        return groupedByCategory.map { (categoryKey, chaptersInCategory) ->
            val books = chaptersInCategory.groupBy {
                val segments = it.remoteUrlPath.trimStart('/').split('/')
                segments.getOrNull(segments.size - 2) ?: categoryKey
            }.map { (bookKey, chaptersInBook) ->
                val firstChapterSegments = chaptersInBook.first().remoteUrlPath.trimStart('/').split('/')
                val testamentKey = firstChapterSegments.getOrNull(firstChapterSegments.size - 3) ?: categoryKey
                AudiobookBook(
                    name = bookKey.toDisplayableName(),
                    testament = testamentKey.toDisplayableName(),
                    chapters = chaptersInBook.sortedByChapterNumber().toImmutableList()
                )
            }
            AudiobookCategory(
                name = categoryKey.toDisplayableName(),
                books = books.sortedBy { it.name }.toImmutableList(),
                isSimpleCategory = books.size == 1 && books.first().name.equals(categoryKey.toDisplayableName(), ignoreCase = true)
            )
        }.toImmutableList()
    }
}