package md.ortodox.ortodoxmd.ui.bible

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.model.bible.VerseWithBookInfo
import md.ortodox.ortodoxmd.data.repository.BibleRepository
import javax.inject.Inject

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val results: List<VerseWithBookInfo>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

@HiltViewModel
class GlobalSearchViewModel @Inject constructor(
    private val repository: BibleRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun search(query: String) {
        if (query.isBlank()) {
            _uiState.value = SearchUiState.Idle
            return
        }

        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            val parsedRef = SearchParser.parse(query)

            try {
                val results = if (parsedRef != null) {
                    // Căutare după referință
                    val verses = repository.getVersesByReference(parsedRef)
                    // Convertim rezultatul la VerseWithBookInfo pentru a avea un tip de date consistent
                    verses.map { v -> VerseWithBookInfo(verse = v, bookNameRo = parsedRef.bookName) }
                } else {
                    // Căutare full-text
                    repository.searchVersesWithBookInfo(query)
                }

                if (results.isNotEmpty()) {
                    _uiState.value = SearchUiState.Success(results)
                } else {
                    _uiState.value = SearchUiState.Error("Niciun rezultat găsit.")
                }
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error("A apărut o eroare: ${e.message}")
            }
        }
    }
}