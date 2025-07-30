package md.ortodox.ortodoxmd.ui.bible

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.model.bible.BibleVerse
import md.ortodox.ortodoxmd.data.repository.BibleRepository
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

// Model de UI pentru a include starea de "marcat"
data class UiVerse(
    val verse: BibleVerse,
    val isBookmarked: Boolean
)

// Starea UI pentru ecranul de versete
data class VersesUiState(
    val isLoading: Boolean = true,
    val bookName: String = "",
    val chapterNumber: Int = 0,
    val verses: List<UiVerse> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
) {
    val filteredVerses: List<UiVerse>
        get() = if (searchQuery.isBlank()) {
            verses
        } else {
            verses.filter {
                it.verse.textRo.contains(searchQuery, ignoreCase = true) ||
                        it.verse.verseNumber.toString() == searchQuery
            }
        }
}

@HiltViewModel
class VersesViewModel @Inject constructor(
    private val repository: BibleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookId: Long = savedStateHandle.get<Long>("bookId") ?: 0L

    // --- START CORECȚIE ---
    // Decodăm numele cărții pentru a transforma '+' înapoi în spații
    private val bookName: String = savedStateHandle.get<String>("bookName")?.let {
        URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
    } ?: "Biblia"
    // --- FINAL CORECȚIE ---

    private val chapterNumber: Int = savedStateHandle.get<Int>("chapterNumber") ?: 0

    private val _uiState = MutableStateFlow(VersesUiState(bookName = bookName, chapterNumber = chapterNumber))
    val uiState: StateFlow<VersesUiState> = _uiState.asStateFlow()

    init {
        loadVerses()
    }

    private fun loadVerses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val versesFromDb = repository.getVerses(bookId, chapterNumber)
                repository.getBookmarkedVerseIds()
                    .map { bookmarkedIds ->
                        versesFromDb.map { verse ->
                            UiVerse(verse = verse, isBookmarked = verse.id in bookmarkedIds)
                        }
                    }
                    .collect { uiVerses ->
                        _uiState.update {
                            it.copy(isLoading = false, verses = uiVerses, error = null)
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Eroare la încărcarea versetelor.") }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleBookmark(verseId: Long) {
        viewModelScope.launch {
            repository.toggleBookmark(verseId)
        }
    }
}