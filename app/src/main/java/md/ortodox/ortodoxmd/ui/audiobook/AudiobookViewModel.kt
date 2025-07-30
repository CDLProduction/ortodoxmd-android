package md.ortodox.ortodoxmd.ui.audiobook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.worker.AudioDownloadWorker
import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity
import md.ortodox.ortodoxmd.data.repository.AudiobookRepository
import javax.inject.Inject

data class AudiobooksUiState(
    val categories: List<AudiobookCategory> = emptyList(),
    val isLoading: Boolean = true,
    val downloadStates: Map<Long, WorkInfo.State> = emptyMap(),
    val downloadProgress: Map<Long, Int> = emptyMap()
)

@HiltViewModel
class AudiobookViewModel @Inject constructor(
    private val repository: AudiobookRepository
) : ViewModel() {

    val uiState: StateFlow<AudiobooksUiState> = combine(
        repository.getAudiobooks(),
        repository.getDownloadWorkInfoFlow()
    ) { audiobooks: List<AudiobookEntity>, workInfos: List<WorkInfo> ->

        // Filtrăm task-urile valide (bazat pe state relevant)
        val validWorkInfos = workInfos.filter { it.state != WorkInfo.State.CANCELLED && it.state != WorkInfo.State.BLOCKED }

        // Creăm map-urile parsând tag-urile pentru a extrage audiobookId
        val downloadStates: Map<Long, WorkInfo.State> = validWorkInfos.associate { workInfo ->
            val audiobookId = extractAudiobookIdFromTags(workInfo.tags)
            audiobookId to workInfo.state
        }.filterKeys { it != -1L }

        val downloadProgress: Map<Long, Int> = validWorkInfos.associate { workInfo ->
            val audiobookId = extractAudiobookIdFromTags(workInfo.tags)
            val progress = workInfo.progress.getInt(AudioDownloadWorker.KEY_PROGRESS, 0)
            audiobookId to progress
        }.filterKeys { it != -1L }

        // Restul logicii rămâne la fel.
        val groupedByBook = audiobooks.groupBy { entity ->
            entity.remoteUrlPath.split("/").getOrNull(3) ?: "necunoscut"
        }

        val books = groupedByBook.map { (bookKey, chapters) ->
            val firstChapter = chapters.first()
            val testamentKey = firstChapter.remoteUrlPath.split("/").getOrNull(2) ?: "necunoscut"

            AudiobookBook(
                name = bookKey.toDisplayableName(),
                testament = testamentKey.toDisplayableName(),
                chapters = chapters.sortedBy { it.id }
            )
        }

        val bibleCategory = AudiobookCategory(
            name = "Biblia",
            books = books.sortedBy { it.name }
        )

        AudiobooksUiState(
            categories = listOf(bibleCategory),
            isLoading = audiobooks.isEmpty() && workInfos.isEmpty(),
            downloadStates = downloadStates,
            downloadProgress = downloadProgress
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AudiobooksUiState(isLoading = true)
    )

    // Funcție helper pentru a extrage ID-ul din tag-uri
    private fun extractAudiobookIdFromTags(tags: Set<String>): Long {
        val customTag = tags.find { it.startsWith("audiobook_download_") }
        return customTag?.substringAfter("audiobook_download_")?.toLongOrNull() ?: -1L
    }

    init {
        viewModelScope.launch {
            repository.syncAudiobooks()
        }
    }

    fun downloadAudiobook(audiobook: AudiobookEntity) {
        repository.startDownload(audiobook)
    }
}