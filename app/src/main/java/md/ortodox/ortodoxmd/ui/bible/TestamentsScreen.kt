package md.ortodox.ortodoxmd.ui.bible

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

// NOU: Ecran pentru a alege între Vechiul și Noul Testament
@Composable
fun TestamentsScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val testaments = listOf(
        "Vechiul Testament" to "1",
        "Noul Testament" to "2"
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(testaments.size) { index ->
            val (name, id) = testaments[index]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("bible/books/$id")
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}