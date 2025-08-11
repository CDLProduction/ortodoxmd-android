package md.ortodox.ortodoxmd.ui.bible

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.ui.design.*

@Composable
fun VersesScreen(
    onBackClick: () -> Unit,
    viewModel: VersesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // OPTIMIZARE: Pas 1 - Creăm o stare derivată pentru lista filtrată.
    // 'remember' asigură că instanța 'derivedStateOf' este păstrată.
    // Blocul de cod se va re-executa DOAR dacă 'uiState.verses' sau 'uiState.searchQuery' se schimbă.
    val filteredVerses by remember(uiState.verses, uiState.searchQuery) {
        derivedStateOf {
            if (uiState.searchQuery.isBlank()) {
                uiState.verses
            } else {
                uiState.verses.filter {
                    it.verse.formattedTextRo.contains(uiState.searchQuery, ignoreCase = true)
                }
            }
        }
    }

    AppScaffold(
        title = "${uiState.bookName} ${uiState.chapterNumber}",
        onBack = onBackClick
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                label = { Text(stringResource(R.string.bible_search_in_chapter)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppPaddings.l, vertical = AppPaddings.s),
                singleLine = true
            )

            when {
                uiState.isLoading -> AppLoading()
                uiState.error != null -> AppError(message = uiState.error!!)
                else -> {
                    // OPTIMIZARE: Pas 2 - Folosim noua listă filtrată, 'filteredVerses'.
                    if (filteredVerses.isEmpty()) {
                        AppEmpty(message = stringResource(R.string.bible_no_results_found))
                    } else {
                        LazyColumn(contentPadding = AppPaddings.content) {
                            items(filteredVerses, key = { it.verse.id }) { uiVerse ->
                                VerseItemWithBookmark(
                                    uiVerse = uiVerse,
                                    onToggleBookmark = { viewModel.toggleBookmark(uiVerse.verse.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Componenta VerseItemWithBookmark rămâne neschimbată.
@Composable
fun VerseItemWithBookmark(
    uiVerse: UiVerse,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = AppPaddings.s),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "${uiVerse.verse.verseNumber}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End,
            modifier = Modifier.width(24.dp)
        )
        Spacer(Modifier.width(AppPaddings.m))
        Text(
            text = uiVerse.verse.formattedTextRo,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(AppPaddings.s))
        IconButton(onClick = onToggleBookmark) {
            Icon(
                imageVector = if (uiVerse.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = stringResource(R.string.bible_save_bookmark),
                tint = if (uiVerse.isBookmarked) MaterialTheme.colorScheme.primary else LocalContentColor.current
            )
        }
    }
}
