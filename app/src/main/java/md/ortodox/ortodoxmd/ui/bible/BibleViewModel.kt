package md.ortodox.ortodoxmd.ui.bible

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.repository.BibleRepository
import md.ortodox.ortodoxmd.data.model.bible.BibleBook
import javax.inject.Inject

@HiltViewModel
class BibleViewModel @Inject constructor(
    private val repository: BibleRepository
) : ViewModel() {
    private val _books = MutableStateFlow<List<BibleBook>?>(null)
    val books: StateFlow<List<BibleBook>?> = _books

    fun fetchBooks(testamentId: Long?) {
        viewModelScope.launch {
            _books.value = repository.getBooks(testamentId)
        }
    }
}