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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@Composable
fun GlobalSearchScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: GlobalSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
    var showHelpDialog by remember { mutableStateOf(false) }

    // **ADAUGAT: Scaffold pentru a putea plasa butonul flotant**
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { showHelpDialog = true }) {
                Icon(Icons.Default.HelpOutline, contentDescription = "Ajutor Căutare")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Ex: Matei 2:15 sau Iisus") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.search(query) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Caută")
            }
            Spacer(Modifier.height(16.dp))

            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Introduceți un text pentru a căuta în Biblie.")
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

    // **ADAUGAT: Dialogul de ajutor care apare la apăsarea butonului**
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            icon = { Icon(Icons.Default.HelpOutline, contentDescription = "Ajutor") },
            title = { Text("Principii de Căutare") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Puteți căuta în două moduri:", style = MaterialTheme.typography.bodyMedium)

                    Text("1. După Referință", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "Introduceți numele cărții (sau o abreviere), urmat de capitol și, opțional, verset.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Exemple: Ioan 3:16, 1 Cor 13, Facerea 1:1-5",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(8.dp))

                    Text("2. După Cuvânt", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "Introduceți orice cuvânt sau expresie pentru a căuta în tot textul Bibliei.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Exemple: mântuire, păstorul cel bun",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Am înțeles")
                }
            }
        )
    }
}