package md.ortodox.ortodoxmd.ui.audiobook

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.WorkInfo
import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudiobookChaptersScreen(
    viewModel: AudiobookViewModel,
    onNavigateToPlayer: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val chapterUiState by viewModel.selectedBookState.collectAsState()
    val mainUiState by viewModel.uiState.collectAsState()
    val isDownloading by viewModel.isDownloading.collectAsState()

    DisposableEffect(Unit) {
        onDispose { viewModel.clearBookSelection() }
    }

    val book = chapterUiState.book
    val hasDownloadableChapters = remember(book, mainUiState.downloadStates) {
        book?.chapters?.any {
            val state = mainUiState.downloadStates[it.id]
            !it.isDownloaded && state != WorkInfo.State.ENQUEUED && state != WorkInfo.State.RUNNING
        } ?: false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book?.name ?: "Încărcare...", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Înapoi") }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isDownloading || hasDownloadableChapters,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                if (isDownloading) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.cancelAllDownloads() },
                        icon = { Icon(Icons.Default.Cancel, "Anulează") },
                        text = { Text("Anulează") },
                        containerColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    ExtendedFloatingActionButton(
                        onClick = { book?.chapters?.let { viewModel.downloadAllChapters(it) } },
                        icon = { Icon(Icons.Default.Download, "Descarcă") },
                        text = { Text("Descarcă") }
                    )
                }
            }
        }
    ) { paddingValues ->
        when {
            chapterUiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
            book != null -> {
                LazyColumn(
                    modifier = Modifier.padding(paddingValues).fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(book.chapters, key = { it.id }) { chapter ->
                        ChapterItem(
                            chapter = chapter,
                            downloadState = mainUiState.downloadStates[chapter.id],
                            progress = mainUiState.downloadProgress[chapter.id] ?: 0,
                            onClick = { onNavigateToPlayer(chapter.id) },
                            onDownload = { viewModel.downloadChapter(chapter) }
                        )
                    }
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Cartea nu a fost găsită.") }
            }
        }
    }
}


@Composable
private fun ChapterItem(
    chapter: AudiobookEntity,
    downloadState: WorkInfo.State?,
    progress: Int,
    onClick: () -> Unit,
    onDownload: () -> Unit
) {
    val isDownloaded = chapter.isDownloaded || downloadState == WorkInfo.State.SUCCEEDED

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                    }
                    downloadState == WorkInfo.State.ENQUEUED -> Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = "În așteptare",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    downloadState == WorkInfo.State.FAILED || downloadState == WorkInfo.State.CANCELLED ->
                        IconButton(onClick = onDownload) {
                            Icon(Icons.Default.Refresh, "Reîncearcă", tint = MaterialTheme.colorScheme.error)
                        }
                    else ->
                        IconButton(onClick = onDownload) {
                            Icon(Icons.Default.Download, "Descarcă", tint = MaterialTheme.colorScheme.secondary)
                        }
                }
            }
        }
    }
}