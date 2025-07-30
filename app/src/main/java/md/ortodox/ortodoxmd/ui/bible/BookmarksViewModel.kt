package md.ortodox.ortodoxmd.ui.bible

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import md.ortodox.ortodoxmd.data.model.bible.VerseWithBookInfo
import md.ortodox.ortodoxmd.data.repository.BibleRepository
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    repository: BibleRepository
) : ViewModel() {

    /**
     * Un StateFlow ce expune lista de semne de carte.
     * Se actualizează automat ori de câte ori se modifică semnele de carte în baza de date.
     */
    val bookmarks: StateFlow<List<VerseWithBookInfo>> = repository.getAllBookmarksWithDetails()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}