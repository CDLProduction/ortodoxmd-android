package md.ortodox.ortodoxmd.ui.bible

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.model.bible.BibleBook
import md.ortodox.ortodoxmd.data.repository.BibleRepository
import javax.inject.Inject

// NOU: Sealed interface pentru a reprezenta stările UI într-un mod robust
sealed interface BooksUiState {
    object Loading : BooksUiState
    data class Success(val books: List<BibleBook>) : BooksUiState
    data class Error(val message: String) : BooksUiState
}

@HiltViewModel
class BooksViewModel @Inject constructor(
    private val repository: BibleRepository
) : ViewModel() {
    // MODIFICAT: StateFlow folosește noul tip BooksUiState
    private val _uiState = MutableStateFlow<BooksUiState>(BooksUiState.Loading)
    val uiState: StateFlow<BooksUiState> = _uiState.asStateFlow()

    fun fetchBooks(testamentId: Long?) {
        viewModelScope.launch {
            _uiState.value = BooksUiState.Loading
            // MODIFICAT: Gestionăm rezultatul de la repository
            repository.getBooks(testamentId)
                .onSuccess { bookList ->
                    _uiState.value = BooksUiState.Success(bookList)
                }
                .onFailure { error ->
                    _uiState.value = BooksUiState.Error(error.localizedMessage ?: "A apărut o eroare la încărcarea cărților.")
                }
        }
    }
}
