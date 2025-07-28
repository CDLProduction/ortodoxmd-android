package md.ortodox.ortodoxmd.ui.audiobook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity
import md.ortodox.ortodoxmd.data.repository.AudiobookRepository
import javax.inject.Inject

/**
 * Starea UI pentru întreaga secțiune de cărți audio.
 */
data class AudiobooksUiState(
    val categories: List<AudiobookCategory> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AudiobookViewModel @Inject constructor(
    private val repository: AudiobookRepository
) : ViewModel() {

    val uiState: StateFlow<AudiobooksUiState> = repository.getAudiobooks()
        .map { flatList ->
            // Procesează lista plată și o grupează ierarhic
            val groupedByBook = flatList.groupBy { entity ->
                // Extrage numele cărții din calea fișierului. Ex: "evanghelia_dupa_ioan"
                entity.remoteUrlPath.split("/").getOrNull(3) ?: "necunoscut"
            }

            val books = groupedByBook.map { (bookKey, chapters) ->
                val firstChapter = chapters.first()
                // Extrage testamentul. Ex: "new_testament"
                val testamentKey = firstChapter.remoteUrlPath.split("/").getOrNull(2) ?: "necunoscut"

                AudiobookBook(
                    name = bookKey.toDisplayableName(),
                    testament = testamentKey.toDisplayableName(),
                    chapters = chapters.sortedBy { it.id }
                )
            }

            // Momentan, avem o singură categorie principală: "Biblia"
            val bibleCategory = AudiobookCategory(
                name = "Biblia",
                books = books.sortedBy { it.name }
            )

            AudiobooksUiState(
                categories = listOf(bibleCategory), // Adaugă și alte categorii aici în viitor
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AudiobooksUiState(isLoading = true)
        )

    init {
        viewModelScope.launch {
            repository.syncAudiobooks()
        }
    }

    fun downloadAudiobook(audiobook: AudiobookEntity) {
        viewModelScope.launch {
            repository.startDownload(audiobook)
        }
    }
}
