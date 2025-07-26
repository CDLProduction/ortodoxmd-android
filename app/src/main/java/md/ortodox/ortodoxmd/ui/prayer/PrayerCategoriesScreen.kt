package md.ortodox.ortodoxmd.ui.prayer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import md.ortodox.ortodoxmd.prayerCategories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerCategoriesScreen(
    navController: NavHostController
) {
    // Adăugăm Scaffold pentru a avea o bară superioară consistentă cu restul aplicației
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorii de Rugăciuni") },
                navigationIcon = {
                    // Buton pentru a te întoarce la ecranul anterior (ex: Home Screen)
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Înapoi"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // Lista de categorii de rugăciuni
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(prayerCategories) { category ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // La click, navighează la ecranul de rugăciuni pentru categoria selectată
                            navController.navigate(category.route)
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}
