package md.ortodox.ortodoxmd.ui.audiobook

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.ui.design.AppCard
import md.ortodox.ortodoxmd.ui.design.AppPaddings
import md.ortodox.ortodoxmd.ui.design.AppScaffold
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AudiobookCategoriesScreen(
    navController: NavController,
    viewModel: AudiobookViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDownloading by viewModel.isDownloading.collectAsStateWithLifecycle()

    AppScaffold(
        title = stringResource(R.string.menu_audiobooks),
        onBack = null, // Fără buton de back pe acest ecran
        floatingActionButton = {
            // --- BUTONUL NOU PENTRU DESCĂRCĂRI ---
            // Este vizibil doar dacă nu sunt descărcări în progres.
            AnimatedVisibility(visible = !isDownloading) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate("downloaded_audiobooks") },
                    icon = { Icon(Icons.Default.DownloadDone, contentDescription = null) },
                    text = { Text(stringResource(R.string.audiobook_downloaded_title)) }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = AppPaddings.content,
            verticalArrangement = Arrangement.spacedBy(AppPaddings.m),
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            items(uiState.categories, key = { it.name }) { category ->
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (category.isSimpleCategory) {
                            val encodedBookName = URLEncoder.encode(category.name, StandardCharsets.UTF_8.toString())
                            navController.navigate("audiobook_chapters/$encodedBookName")
                        } else {
                            val hasMultipleTestaments = category.books.map { it.testament }.distinct().size > 1
                            if (hasMultipleTestaments) {
                                val encodedCategoryName = URLEncoder.encode(category.name, StandardCharsets.UTF_8.toString())
                                navController.navigate("audiobook_testaments/$encodedCategoryName")
                            } else {
                                val testamentName = category.books.firstOrNull()?.testament ?: category.name
                                val encodedTestamentName = URLEncoder.encode(testamentName, StandardCharsets.UTF_8.toString())
                                navController.navigate("audiobook_books/$encodedTestamentName")
                            }
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
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = AppPaddings.l),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
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