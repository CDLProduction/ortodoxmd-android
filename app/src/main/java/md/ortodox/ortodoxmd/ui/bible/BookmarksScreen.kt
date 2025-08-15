package md.ortodox.ortodoxmd.ui.bible

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.ui.design.AppCard
import md.ortodox.ortodoxmd.ui.design.AppEmpty
import md.ortodox.ortodoxmd.ui.design.AppPaddings
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun BookmarksScreen(
    navController: NavHostController,
    viewModel: BookmarksViewModel = hiltViewModel()
) {
    val bookmarks by viewModel.bookmarks.collectAsState()

    if (bookmarks.isEmpty()) {
        // REFACTORIZAT: Folosim AppEmpty.
        AppEmpty(message = stringResource(R.string.bible_no_bookmarks))
    } else {
        LazyColumn(
            contentPadding = AppPaddings.content,
            verticalArrangement = Arrangement.spacedBy(AppPaddings.s)
        ) {
            items(bookmarks, key = { it.verse.id }) { item ->
                // REFACTORIZAT: Folosim AppCard.
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val encodedBookName = URLEncoder.encode(item.bookNameRo, StandardCharsets.UTF_8.toString())
                        val route = "bible/verses/${item.verse.bookId}/$encodedBookName/${item.verse.chapterNumber}"
                        navController.navigate(route)
                    }
                ) {
                    // Păstrăm VerseItem, deoarece este o componentă specifică.
                    VerseItem(
                        verseNumber = item.verse.verseNumber.toString(),
                        verseText = item.verse.formattedTextRo,
                        reference = "${item.bookNameRo} ${item.verse.chapterNumber}",
                        modifier = Modifier.padding(AppPaddings.l)
                    )
                }
            }
        }
    }
}
