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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
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
    val hasDownloadableChapters = remember(book, mainUiState) {
        book?.chapters?.any {
            !it.isDownloaded
        } ?: false
    }
    val hasDeletableChapters = remember(book, mainUiState) {
        book?.chapters?.any { it.isDownloaded } ?: false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book?.name ?: stringResource(R.string.loading), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isDownloading || hasDeletableChapters || hasDownloadableChapters,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                when {
                    isDownloading -> ExtendedFloatingActionButton(
                        onClick = { viewModel.cancelAllDownloads() },
                        icon = { Icon(Icons.Default.Cancel, stringResource(R.string.cancel)) },
                        text = { Text(stringResource(R.string.cancel)) },
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                    hasDeletableChapters -> ExtendedFloatingActionButton(
                        onClick = { book?.chapters?.let { viewModel.deleteAllDownloadedChapters(it) } },
                        icon = { Icon(Icons.Default.Delete, stringResource(R.string.delete)) },
                        text = { Text(stringResource(R.string.delete_downloads)) },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                    hasDownloadableChapters -> ExtendedFloatingActionButton(
                        onClick = { book?.chapters?.let { viewModel.downloadAllChapters(it) } },
                        icon = { Icon(Icons.Default.Download, stringResource(R.string.download)) },
                        text = { Text(stringResource(R.string.download)) }
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
                            onDownload = { viewModel.downloadChapter(chapter) },
                            onDelete = { viewModel.deleteChapter(chapter) }
                        )
                    }
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(stringResource(R.string.book_not_found)) }
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
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    val isDownloaded = chapter.isDownloaded

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Headset,
                contentDescription = stringResource(R.string.chapter_icon_desc),
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
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isDownloaded -> Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.downloaded),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                    downloadState == WorkInfo.State.RUNNING -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val animatedProgress by animateFloatAsState(targetValue = progress / 100f, label = "progressAnimation")
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Text(text = "$progress%", fontSize = 10.sp)
                    }
                    // --> AICI ESTE LOGICA PENTRU STAREA "ÎN AȘTEPTARE" <--
                    downloadState == WorkInfo.State.ENQUEUED -> Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = stringResource(R.string.waiting),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    downloadState == WorkInfo.State.FAILED || downloadState == WorkInfo.State.CANCELLED ->
                        IconButton(onClick = onDownload) {
                            Icon(Icons.Default.Refresh, stringResource(R.string.retry), tint = MaterialTheme.colorScheme.error)
                        }
                    else ->
                        IconButton(onClick = onDownload) {
                            Icon(Icons.Default.Download, stringResource(R.string.download), tint = MaterialTheme.colorScheme.secondary)
                        }
                }
            }
        }
    }
}