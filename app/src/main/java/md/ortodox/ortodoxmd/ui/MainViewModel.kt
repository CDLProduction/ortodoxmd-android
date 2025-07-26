package md.ortodox.ortodoxmd.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.model.bible.BookmarkWithDetails
import md.ortodox.ortodoxmd.data.repository.BibleRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: BibleRepository
) : ViewModel() {
    private val _bookmarkNavigator = MutableSharedFlow<BookmarkWithDetails>(replay = 0)
    val bookmarkNavigator = _bookmarkNavigator.asSharedFlow()

    fun onBookmarkClicked() {
        viewModelScope.launch {
            repository.getBookmarkWithDetails()?.let {
                _bookmarkNavigator.emit(it)
            }
        }
    }
}