package md.ortodox.ortodoxmd.ui.audiobook

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity

@Composable
fun AudiobookChaptersScreen(
    navController: NavController,
    book: AudiobookBook?,
    onDownloadClick: (AudiobookEntity) -> Unit
) {
    if (book == null) {
        // Afișează un mesaj dacă, din orice motiv, cartea nu este găsită
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Cartea nu a fost găsită.")
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(book.chapters, key = { it.id }) { chapter ->
            ChapterItem(
                chapter = chapter,
                onClick = {
                    navController.navigate("audiobook_player/${chapter.id}")
                },
                onDownload = { onDownloadClick(chapter) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterItem(
    chapter: AudiobookEntity,
    onClick: () -> Unit,
    onDownload: () -> Unit
) {
    val cardAlpha = if (chapter.isDownloaded) 1f else 0.7f

    Card(
        onClick = onClick,
        enabled = true,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = cardAlpha)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Headset,
                contentDescription = "Icoană Capitol",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chapter.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = chapter.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(16.dp))

            if (chapter.isDownloaded) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Descărcat",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                IconButton(onClick = onDownload) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Descarcă",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
