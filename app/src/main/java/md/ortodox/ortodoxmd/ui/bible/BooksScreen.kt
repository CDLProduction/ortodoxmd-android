package md.ortodox.ortodoxmd.ui.bible

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.ui.design.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun BooksScreen(
    navController: NavHostController,
    testamentId: Long?,
    viewModel: BooksViewModel = hiltViewModel()
) {
    LaunchedEffect(testamentId) {
        viewModel.fetchBooks(testamentId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val title = when (testamentId) {
        1L -> stringResource(R.string.bible_old_testament)
        2L -> stringResource(R.string.bible_new_testament)
        else -> stringResource(R.string.bible_books_title)
    }

    // CORECTAT: Am eliminat parametrul 'modifier' din apelul AppScaffold.
    AppScaffold(
        title = title,
        onBack = { navController.popBackStack() }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = uiState) {
                is BooksUiState.Loading -> AppLoading()
                is BooksUiState.Error -> AppError(message = state.message)
                is BooksUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = AppPaddings.content,
                        verticalArrangement = Arrangement.spacedBy(AppPaddings.s)
                    ) {
                        items(state.books, key = { it.id }) { book ->
                            AppCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    val encodedBookName = URLEncoder.encode(book.nameRo, StandardCharsets.UTF_8.toString())
                                    navController.navigate("bible/chapters/${book.id}/$encodedBookName")
                                }
                            ) {
                                Text(
                                    text = book.nameRo,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(AppPaddings.l)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
