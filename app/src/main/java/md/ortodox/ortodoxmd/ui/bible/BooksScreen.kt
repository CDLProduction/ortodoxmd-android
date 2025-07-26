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
import md.ortodox.ortodoxmd.data.model.bible.BibleBook

@Composable
fun BooksScreen(testamentId: Long, modifier: Modifier = Modifier) {
    val viewModel: BooksViewModel = hiltViewModel()
    viewModel.fetchBooks(testamentId)
    val books = viewModel.books.collectAsState().value

    Column(modifier = modifier.padding(16.dp)) {
        books?.forEach { book ->
            Text(text = book.nameRo, style = MaterialTheme.typography.bodyLarge)
        } ?: Text("Încărcare...")
    }
}