// AudiobookBooksScreen.kt (corectat)
package md.ortodox.ortodoxmd.ui.audiobook

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudiobookBooksScreen(
    navController: NavController,
    testamentName: String,
    books: List<AudiobookBook>
) {
    Log.d("AudiobookBooksScreen", "Rendering with testamentName: $testamentName")
    val booksInTestament = books.filter { it.testament == testamentName }

    Scaffold(
        topBar = {
            Log.d("AudiobookBooksScreen", "TopAppBar rendering with testamentName: $testamentName")
            TopAppBar(
                title = { Text(testamentName.ifEmpty { stringResource(R.string.books) }) },
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
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(paddingValues)
        ) {
            items(booksInTestament, key = { it.name }) { book ->
                val interactionSource = remember { MutableInteractionSource() }
                val isHovered by interactionSource.collectIsHoveredAsState()
                val scale = animateFloatAsState(
                    targetValue = if (isHovered) 1.02f else 1f,
                    animationSpec = tween(durationMillis = 200)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(scale.value)
                        .padding(vertical = 4.dp)
                        .hoverable(interactionSource = interactionSource),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isHovered) 8.dp else 2.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable(
                                enabled = true,
                                onClickLabel = stringResource(R.string.navigate_to_book, book.name),
                                onClick = {
                                    val encodedBookName = URLEncoder.encode(book.name, StandardCharsets.UTF_8.toString())
                                    navController.navigate("audiobook_chapters/$encodedBookName")
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.LibraryBooks,
                            contentDescription = stringResource(R.string.book_icon_desc),
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = book.name,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isHovered) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = stringResource(R.string.navigate),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}