package md.ortodox.ortodoxmd.ui.bible

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersesScreen(
    bookId: Long,
    bookName: String,
    chapterNumber: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: VersesViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchVerses(bookId, chapterNumber)
    }

    LaunchedEffect(searchQuery) {
        viewModel.searchVerses(searchQuery, bookId, chapterNumber)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(bookName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Înapoi")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveBookmark(bookId, chapterNumber, 0L) }) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = "Salvează ca semn de carte")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Caută în Biblie...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // CORECLAT: Blocul 'when' este acum exhaustiv și corect.
            when (val state = uiState) {
                is VersesUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is VersesUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                is VersesUiState.ChapterSuccess -> {
                    if (state.verses.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Capitol gol sau inexistent.", modifier = Modifier.padding(16.dp))
                        }
                    } else {
                        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                            item {
                                Text(
                                    text = "Capitolul $chapterNumber",
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }
                            items(state.verses, key = { it.id }) { verse ->
                                VerseItem(
                                    verseNumber = verse.verseNumber.toString(),
                                    verseText = verse.formattedTextRo
                                )
                            }
                        }
                    }
                }
                is VersesUiState.SearchSuccess -> {
                    if (state.results.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Niciun rezultat găsit.", modifier = Modifier.padding(16.dp))
                        }
                    } else {
                        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                            items(state.results, key = { it.verse.id }) { result ->
                                VerseItem(
                                    verseNumber = result.verse.verseNumber.toString(),
                                    verseText = result.verse.formattedTextRo,
                                    reference = "${result.bookNameRo} ${result.verse.chapterNumber}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VerseItem(
    verseNumber: String,
    verseText: String,
    reference: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = verseNumber,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(24.dp)
        )
        Column {
            if (reference != null) {
                Text(
                    text = reference,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(
                text = verseText,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.fontSize * 1.5
            )
        }
    }
}