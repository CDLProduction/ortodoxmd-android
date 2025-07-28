package md.ortodox.ortodoxmd.ui.audiobook

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AudiobookBooksScreen(
    navController: NavController,
    testamentName: String,
    books: List<AudiobookBook>
) {
    val booksInTestament = books.filter { it.testament == testamentName }

    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(booksInTestament, key = { it.name }) { book ->
            ListItem(
                headlineContent = { Text(book.name) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                modifier = Modifier.clickable {
                    // Navighează la ecranul cu capitole, trimițând numele cărții ca argument
                    val encodedBookName = URLEncoder.encode(book.name, StandardCharsets.UTF_8.toString())
                    navController.navigate("audiobook_chapters/$encodedBookName")
                }
            )
        }
    }
}
