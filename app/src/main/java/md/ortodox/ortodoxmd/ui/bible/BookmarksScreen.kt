package md.ortodox.ortodoxmd.ui.bible
import md.ortodox.ortodoxmd.R

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun BookmarksScreen(
    navController: NavHostController,
    viewModel: BookmarksViewModel = hiltViewModel()
) {
    val bookmarks by viewModel.bookmarks.collectAsState()

    if (bookmarks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_bookmarks), modifier = Modifier.padding(16.dp))
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(bookmarks, key = { it.verse.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        val encodedBookName = URLEncoder.encode(item.bookNameRo, StandardCharsets.UTF_8.toString())
                        val route = "bible/verses/${item.verse.bookId}/$encodedBookName/${item.verse.chapterNumber}"
                        navController.navigate(route)
                    },
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    VerseItem(
                        verseNumber = item.verse.verseNumber.toString(),
                        verseText = item.verse.formattedTextRo,
                        reference = "${item.bookNameRo} ${item.verse.chapterNumber}",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}