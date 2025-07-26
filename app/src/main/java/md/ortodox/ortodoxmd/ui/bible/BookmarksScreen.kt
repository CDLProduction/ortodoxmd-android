package md.ortodox.ortodoxmd.ui.bible

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import md.ortodox.ortodoxmd.data.model.bible.VerseWithBookInfo
import md.ortodox.ortodoxmd.data.repository.BibleRepository
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(repository: BibleRepository) : ViewModel() {
    val bookmarks: StateFlow<List<VerseWithBookInfo>> = repository.getAllBookmarksWithDetails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@Composable
fun BookmarksScreen(
    navController: NavHostController,
    viewModel: BookmarksViewModel = hiltViewModel()
) {
    val bookmarks by viewModel.bookmarks.collectAsState()

    if (bookmarks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nu aveți niciun semn de carte salvat.", modifier = Modifier.padding(16.dp))
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(bookmarks, key = { it.verse.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        val route = "bible/verses/${item.verse.bookId}/${item.bookNameRo}/${item.verse.chapterNumber}"
                        navController.navigate(route)
                    },
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    // Apelul către VerseItem, care acum este public și vizibil
                    VerseItem(
                        verseNumber = item.verse.verseNumber.toString(),
                        verseText = item.verse.formattedTextRo,
                        reference = "${item.bookNameRo} ${item.verse.chapterNumber}",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
