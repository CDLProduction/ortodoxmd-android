package md.ortodox.ortodoxmd.ui.bible

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.model.bible.BibleBookmark
import md.ortodox.ortodoxmd.data.model.bible.BibleVerse
import md.ortodox.ortodoxmd.data.model.bible.VerseWithBookInfo
import md.ortodox.ortodoxmd.data.repository.BibleRepository
import javax.inject.Inject

sealed interface VersesUiState {
    object Loading : VersesUiState
    data class ChapterSuccess(val verses: List<BibleVerse>) : VersesUiState
    data class SearchSuccess(val results: List<VerseWithBookInfo>) : VersesUiState
    data class Error(val message: String) : VersesUiState
}

@HiltViewModel
class VersesViewModel @Inject constructor(
    private val repository: BibleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<VersesUiState>(VersesUiState.Loading)
    val uiState: StateFlow<VersesUiState> = _uiState.asStateFlow()

    // NOU: Canal pentru a trimite mesaje one-time (ex: Snackbar)
    private val _snackbarChannel = Channel<String>()
    val snackbarFlow = _snackbarChannel.receiveAsFlow()

    private var searchJob: Job? = null

    fun fetchVerses(bookId: Long, chapterNumber: Int) {
        searchJob?.cancel()
        viewModelScope.launch {
            _uiState.value = VersesUiState.Loading
            try {
                val verses = repository.getVerses(bookId, chapterNumber)
                _uiState.value = VersesUiState.ChapterSuccess(verses)
            } catch (e: Exception) {
                _uiState.value = VersesUiState.Error("Nu s-au putut încărca versetele: ${e.message}")
            }
        }
    }

    fun searchVerses(query: String, bookId: Long, chapterNumber: Int) {
        searchJob?.cancel()
        if (query.isBlank()) {
            fetchVerses(bookId, chapterNumber)
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            _uiState.value = VersesUiState.Loading
            try {
                val results = repository.searchVersesWithBookInfo(query)
                _uiState.value = VersesUiState.SearchSuccess(results)
            } catch (e: Exception) {
                _uiState.value = VersesUiState.Error("Căutarea a eșuat: ${e.message}")
            }
        }
    }

    fun saveBookmark(bookId: Long, chapterNumber: Int, verseId: Long) {
        viewModelScope.launch {
            val newBookmark = BibleBookmark(bookId = bookId, chapterNumber = chapterNumber.toLong(), verseId = verseId)
            repository.saveBookmark(newBookmark)
            // NOU: Trimite un mesaj de confirmare către UI
            _snackbarChannel.send("Semn de carte salvat!")
        }
    }
}