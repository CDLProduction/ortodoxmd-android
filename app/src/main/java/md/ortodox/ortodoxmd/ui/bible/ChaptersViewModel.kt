package md.ortodox.ortodoxmd.ui.bible

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.repository.BibleRepository
import md.ortodox.ortodoxmd.data.model.bible.BibleChapter
import javax.inject.Inject

@HiltViewModel
class ChaptersViewModel @Inject constructor(
    private val repository: BibleRepository
) : ViewModel() {
    private val _chapters = MutableStateFlow<List<BibleChapter>?>(null)
    val chapters: StateFlow<List<BibleChapter>?> = _chapters

    fun fetchChapters(bookId: Long) {
        viewModelScope.launch {
            _chapters.value = repository.getChapters(bookId)
        }
    }
}