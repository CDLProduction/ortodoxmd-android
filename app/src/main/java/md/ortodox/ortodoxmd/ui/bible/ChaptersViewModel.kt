package md.ortodox.ortodoxmd.ui.bible

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.model.bible.BibleChapter
import md.ortodox.ortodoxmd.data.repository.BibleRepository
import javax.inject.Inject

// O clasă de stare pentru a comunica mai clar starea ecranului
data class ChaptersUiState(
    val isLoading: Boolean = true,
    val chapters: List<BibleChapter> = emptyList()
)

@HiltViewModel
class ChaptersViewModel @Inject constructor(
    private val repository: BibleRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChaptersUiState())
    val uiState = _uiState.asStateFlow()

    fun fetchChapters(bookId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val chaptersFromRepo = repository.getChapters(bookId)

            // **AICI ESTE CORECȚIA PENTRU SORTARE**
            val sortedChapters = chaptersFromRepo.sortedBy { it.chapterNumber }

            _uiState.update { it.copy(isLoading = false, chapters = sortedChapters) }
        }
    }
}