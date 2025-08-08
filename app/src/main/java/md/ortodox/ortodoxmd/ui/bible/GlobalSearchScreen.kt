package md.ortodox.ortodoxmd.ui.bible

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import md.ortodox.ortodoxmd.R

@Composable
fun GlobalSearchScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: GlobalSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
    var showHelpDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { showHelpDialog = true }) {
                Icon(Icons.Default.HelpOutline, contentDescription = stringResource(R.string.bible_search_help_icon_desc))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).fillMaxSize().padding(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(stringResource(R.string.bible_search_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.search(query) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.bible_search_button))
            }
            Spacer(Modifier.height(16.dp))

            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.bible_search_idle_text))
                    }
                }
                is SearchUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is SearchUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is SearchUiState.Success -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            icon = { Icon(Icons.Default.HelpOutline, contentDescription = stringResource(R.string.bible_search_help_icon_desc)) },
            title = { Text(stringResource(R.string.bible_search_help_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.bible_search_help_intro), style = MaterialTheme.typography.bodyMedium)

                    Text(stringResource(R.string.bible_search_help_ref_title), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.bible_search_help_ref_desc), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.bible_search_help_ref_examples), style = MaterialTheme.typography.bodySmall)

                    Spacer(Modifier.height(8.dp))

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