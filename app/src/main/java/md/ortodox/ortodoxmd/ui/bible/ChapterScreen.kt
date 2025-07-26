package md.ortodox.ortodoxmd.ui.bible

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import md.ortodox.ortodoxmd.data.model.bible.BibleChapter

@Composable
fun ChaptersScreen(bookId: Long, modifier: Modifier = Modifier) {
    val viewModel: ChaptersViewModel = hiltViewModel()
    viewModel.fetchChapters(bookId)
    val chapters = viewModel.chapters.collectAsState().value

    Column(modifier = modifier.padding(16.dp)) {
        chapters?.forEach { chapter ->
            Text(text = "Capitol ${chapter.chapterNumber}", style = MaterialTheme.typography.bodyLarge)
        } ?: Text("Încărcare...")
    }
}