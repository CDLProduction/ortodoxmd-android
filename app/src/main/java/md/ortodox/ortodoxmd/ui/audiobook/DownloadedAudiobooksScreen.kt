package md.ortodox.ortodoxmd.ui.audiobook

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.ui.design.AppCard
import md.ortodox.ortodoxmd.ui.design.AppEmpty
import md.ortodox.ortodoxmd.ui.design.AppLoading
import md.ortodox.ortodoxmd.ui.design.AppPaddings
import md.ortodox.ortodoxmd.ui.design.AppScaffold
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun DownloadedAudiobooksScreen(
    navController: NavController,
    viewModel: AudiobookViewModel = hiltViewModel()
) {
    val uiState by viewModel.downloadedUiState.collectAsStateWithLifecycle()

    AppScaffold(
        title = stringResource(R.string.audiobook_downloaded_title), // Va trebui să adaugi acest string în strings.xml
        onBack = { navController.popBackStack() }
    ) { paddingValues ->

        if (uiState.isLoading) {
            AppLoading(modifier = Modifier.padding(paddingValues))
            return@AppScaffold
        }

        if (uiState.categories.isEmpty()) {
            AppEmpty(
                message = stringResource(R.string.audiobook_no_downloads), // Și acest string
                modifier = Modifier.padding(paddingValues)
            )
            return@AppScaffold
        }

        LazyColumn(
            contentPadding = AppPaddings.content,
            verticalArrangement = Arrangement.spacedBy(AppPaddings.m),
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            uiState.categories.forEach { category ->
                // Adăugăm un titlu pentru fiecare categorie, dacă există mai multe
                if (uiState.categories.size > 1) {
                    item {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = AppPaddings.l, bottom = AppPaddings.s)
                        )
                    }
                }

                // Listăm cărțile din categorie
                items(category.books, key = { it.name }) { book ->
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val encodedBookName = URLEncoder.encode(book.name, StandardCharsets.UTF_8.toString())
                            navController.navigate("audiobook_chapters/$encodedBookName")
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(AppPaddings.l),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.LibraryBooks,
                                contentDescription = stringResource(R.string.audiobook_book_icon_desc),
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = book.name,
                                modifier = Modifier.weight(1f).padding(horizontal = AppPaddings.l),
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = stringResource(R.string.common_navigate),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}