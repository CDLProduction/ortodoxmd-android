package md.ortodox.ortodoxmd.ui.bible

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@Composable
fun ChaptersScreen(
    navController: NavHostController,
    bookId: Long,
    bookName: String, // NOU: Primim numele cărții pentru a-l pasa mai departe
    modifier: Modifier = Modifier
) {
    val viewModel: ChaptersViewModel = hiltViewModel()

    // MODIFICAT: Folosim LaunchedEffect
    LaunchedEffect(bookId) {
        viewModel.fetchChapters(bookId)
    }

    val chapters = viewModel.chapters.collectAsState().value

    if (chapters == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        // MODIFICAT: Folosim LazyColumn
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chapters, key = { it.id }) { chapter ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { // NOU: Navigare la ecranul cu versete
                            navController.navigate("bible/verses/${bookId}/${bookName}/${chapter.chapterNumber}")
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "Capitolul ${chapter.chapterNumber}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}