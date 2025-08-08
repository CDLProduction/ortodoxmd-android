package md.ortodox.ortodoxmd.ui.prayer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.SubDrawerItem // Importul este corect, presupunând că a fost modificat în MainActivity

// CORECTAT: Definim lista folosind ID-uri de resurse, conform noii structuri a clasei SubDrawerItem
private val prayerCategoriesForScreen = listOf(
    SubDrawerItem(R.string.prayer_cat_morning, "prayer/morning"),
    SubDrawerItem(R.string.prayer_cat_evening, "prayer/evening"),
    SubDrawerItem(R.string.prayer_cat_illness, "prayer/for_illness"),
    SubDrawerItem(R.string.prayer_cat_general, "prayer/general")
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerCategoriesScreen(
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.prayer_categories_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) { // navController.navigateUp() e adesea mai bun
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(prayerCategoriesForScreen) { category ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate(category.route)
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        // CORECTAT: Folosim stringResource și noul nume de proprietate 'titleResId'
                        text = stringResource(id = category.titleResId),
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