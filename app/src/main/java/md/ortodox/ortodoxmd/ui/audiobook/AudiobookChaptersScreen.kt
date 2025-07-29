package md.ortodox.ortodoxmd.ui.audiobook

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.work.WorkInfo
import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity

@Composable
fun AudiobookChaptersScreen(
    navController: NavController,
    book: AudiobookBook?,
    viewModel: AudiobookViewModel // Acum primește ViewModel-ul ca parametru
) {
    if (book == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Cartea nu a fost găsită.")
        }
        return
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(book.chapters, key = { it.id }) { chapter ->
            val downloadState = uiState.downloadStates[chapter.id]
            val progress = uiState.downloadProgress[chapter.id] ?: 0

            ChapterItem(
                chapter = chapter,
                downloadState = downloadState,
                progress = progress,
                onClick = { navController.navigate("audiobook_player/${chapter.id}") },
                onDownload = { viewModel.downloadAudiobook(chapter) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterItem(
    chapter: AudiobookEntity,
    downloadState: WorkInfo.State?,
    progress: Int,
    onClick: () -> Unit,
    onDownload: () -> Unit
) {
    val isDownloaded = chapter.isDownloaded || downloadState == WorkInfo.State.SUCCEEDED
    val cardAlpha = if (isDownloaded) 1f else 0.8f

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
                    maxLines = 2,
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

            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                when {
                    isDownloaded -> Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Descărcat",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    downloadState == WorkInfo.State.RUNNING -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Text(text = "$progress%", style = MaterialTheme.typography.labelSmall)
                    }
                    downloadState == WorkInfo.State.ENQUEUED -> Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = "În așteptare",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    downloadState == WorkInfo.State.FAILED || downloadState == WorkInfo.State.CANCELLED -> Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Eroare",
                        tint = MaterialTheme.colorScheme.error
                    )
                    else -> IconButton(onClick = onDownload) {
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
}
