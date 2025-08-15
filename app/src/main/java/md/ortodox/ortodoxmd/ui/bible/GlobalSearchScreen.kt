package md.ortodox.ortodoxmd.ui.bible

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.ui.design.*

@Composable
fun GlobalSearchScreen(
    navController: NavHostController,
    viewModel: GlobalSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
    var showHelpDialog by remember { mutableStateOf(false) }

    // CORECTAT: Am eliminat parametrul 'modifier' din apelul AppScaffold.
    AppScaffold(
        title = stringResource(R.string.bible_tab_search),
        floatingActionButton = {
            FloatingActionButton(onClick = { showHelpDialog = true }) {
                Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = stringResource(R.string.bible_search_help_icon_desc))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(AppPaddings.l)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(stringResource(R.string.bible_search_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(AppPaddings.s))
            Button(
                onClick = { viewModel.search(query) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.bible_search_button))
            }
            Spacer(Modifier.height(AppPaddings.l))

            when (val state = uiState) {
                is SearchUiState.Idle -> AppEmpty(message = stringResource(R.string.bible_search_idle_text))
                is SearchUiState.Loading -> AppLoading()
                is SearchUiState.Error -> AppError(message = state.message)
                is SearchUiState.Success -> {
                    if (state.results.isEmpty()) {
                        AppEmpty(message = stringResource(R.string.bible_search_no_results))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(AppPaddings.s)) {
                            items(state.results) { result ->
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

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            icon = { Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = stringResource(R.string.bible_search_help_icon_desc)) },
            title = { Text(stringResource(R.string.bible_search_help_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppPaddings.s)) {
                    Text(stringResource(R.string.bible_search_help_intro), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.bible_search_help_ref_title), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.bible_search_help_ref_desc), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.bible_search_help_ref_examples), style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(AppPaddings.s))
                    Text(stringResource(R.string.bible_search_help_word_title), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.bible_search_help_word_desc), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.bible_search_help_word_examples), style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text(stringResource(R.string.bible_search_help_confirm))
                }
            }
        )
    }
}
