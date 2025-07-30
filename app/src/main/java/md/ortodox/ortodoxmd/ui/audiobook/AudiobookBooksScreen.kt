package md.ortodox.ortodoxmd.ui.audiobook

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.compose.material3.MaterialTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudiobookBooksScreen(
    navController: NavController,
    testamentName: String,
    books: List<AudiobookBook>
) {
    Log.d("AudiobookBooksScreen", "Rendering with testamentName: $testamentName")
    val booksInTestament = books.filter { it.testament == testamentName }

    // Forțează recompunerea și verifică starea
    LaunchedEffect(testamentName) {
        Log.d("AudiobookBooksScreen", "LaunchedEffect triggered with testamentName: $testamentName")
    }

    Scaffold(
        topBar = {
            Log.d("AudiobookBooksScreen", "TopAppBar rendering with testamentName: $testamentName")
            TopAppBar(
                title = { Text(testamentName.ifEmpty { "Cărți" }) },
                navigationIcon = {
                    Log.d("AudiobookBooksScreen", "Navigation icon rendering")
                    IconButton(
                        onClick = {
                            Log.d("AudiobookBooksScreen", "Back button clicked")
                            navController.popBackStack()
                        },
                        enabled = true
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Înapoi",
                            tint = MaterialTheme.colorScheme.onBackground // Culoare dinamică bazată pe temă
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(paddingValues)
        ) {
            items(booksInTestament, key = { it.name }) { book ->
                ListItem(
                    headlineContent = { Text(book.name) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                    modifier = Modifier.clickable {
                        val encodedBookName = URLEncoder.encode(book.name, StandardCharsets.UTF_8.toString())
                        navController.navigate("audiobook_chapters/$encodedBookName")
                    }
                )
            }
        }
    }
}