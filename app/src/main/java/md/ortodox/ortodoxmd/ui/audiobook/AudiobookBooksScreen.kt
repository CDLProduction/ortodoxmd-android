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
fun AudiobookBooksScreen(
    navController: NavController,
    testamentName: String,
    books: List<AudiobookBook>
) {
    val booksInTestament = books.filter { it.testament == testamentName }

    // REFACTORIZAT: Folosim AppScaffold.
    AppScaffold(
        title = testamentName.ifEmpty { stringResource(R.string.common_books) },
        onBack = { navController.popBackStack() }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = AppPaddings.content,
            verticalArrangement = Arrangement.spacedBy(AppPaddings.m),
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            items(booksInTestament, key = { it.name }) { book ->
                // REFACTORIZAT: Folosim AppCard.
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
                            style = MaterialTheme.typography.titleMedium
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
