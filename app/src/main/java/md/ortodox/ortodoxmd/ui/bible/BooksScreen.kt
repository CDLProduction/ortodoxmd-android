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

// MODIFICAT: Acest ecran va afișa cărțile unui testament specific.
@Composable
fun BooksScreen(
    navController: NavHostController,
    testamentId: Long?,
    modifier: Modifier = Modifier
) {
    val viewModel: BooksViewModel = hiltViewModel()

    // MODIFICAT: Apelul se face într-un LaunchedEffect pentru a evita execuții multiple.
    LaunchedEffect(testamentId) {
        viewModel.fetchBooks(testamentId)
    }

    val books = viewModel.books.collectAsState().value

    if (books == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        // MODIFICAT: Folosim LazyColumn pentru performanță
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(books, key = { it.id }) { book ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { // NOU: Navigare la ecranul cu capitole
                            navController.navigate("bible/chapters/${book.id}/${book.nameRo}")
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = book.nameRo,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}