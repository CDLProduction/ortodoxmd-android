package md.ortodox.ortodoxmd.ui.audiobook

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.ui.design.AppCard
import md.ortodox.ortodoxmd.ui.design.AppPaddings
import md.ortodox.ortodoxmd.ui.design.AppScaffold
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AudiobookCategoriesScreen(navController: NavController, categories: List<AudiobookCategory>, categoryName: String) {
    AppScaffold(
        title = stringResource(R.string.audiobook_categories_title, categoryName),
        onBack = { navController.popBackStack() }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = AppPaddings.content,
            verticalArrangement = Arrangement.spacedBy(AppPaddings.m),
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            items(categories, key = { it.name }) { category ->
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        // --- LOGICA DE NAVIGAȚIE DINAMICĂ ---
                        if (category.isSimpleCategory) {
                            // Dacă este o categorie simplă, navigăm direct la capitole.
                            // Numele "cărții" este același cu numele categoriei.
                            val encodedBookName = URLEncoder.encode(category.name, StandardCharsets.UTF_8.toString())
                            navController.navigate("audiobook_chapters/$encodedBookName")
                        } else {
                            // Altfel, navigăm la testamente, ca înainte.
                            val encodedCategoryName = URLEncoder.encode(category.name, StandardCharsets.UTF_8.toString())
                            navController.navigate("audiobook_testaments/${category.name}?categoryName=$encodedCategoryName")
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(AppPaddings.l),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.LibraryBooks,
                            contentDescription = stringResource(R.string.audiobook_category_icon_desc),
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = category.name,
                            modifier = Modifier.weight(1f).padding(start = AppPaddings.l),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.common_navigate, category.name),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}
