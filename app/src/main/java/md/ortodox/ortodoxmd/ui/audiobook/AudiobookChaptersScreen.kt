package md.ortodox.ortodoxmd.ui.audiobook

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity
import md.ortodox.ortodoxmd.ui.MainViewModel
import md.ortodox.ortodoxmd.ui.design.AppEmpty
import md.ortodox.ortodoxmd.ui.design.AppLoading
import md.ortodox.ortodoxmd.ui.design.AppPaddings
import md.ortodox.ortodoxmd.ui.design.AppScaffold

@Composable
fun AudiobookChaptersScreen(
    viewModel: AudiobookViewModel,
    onNavigateToPlayer: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    // Obținem ViewModel-ul global pentru a ști ce piesă rulează
    val mainViewModel: MainViewModel = hiltViewModel()
    val miniPlayerState by mainViewModel.miniPlayerState.collectAsStateWithLifecycle()
    val currentPlayingId = miniPlayerState.currentTrackId

    val chapterUiState by viewModel.selectedBookState.collectAsStateWithLifecycle()
    val mainUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDownloading by viewModel.isDownloading.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose { viewModel.clearBookSelection() }
    }

    val book = chapterUiState.book

    // Folosim derivedStateOf pentru a recalcula vizibilitatea butoanelor
    // DOAR atunci când rezultatul (true/false) se schimbă efectiv.
    val hasDownloadableChapters by remember(book) {
        derivedStateOf { book?.chapters?.any { !it.isDownloaded } ?: false }
    }
    val hasDeletableChapters by remember(book, mainUiState.downloadStates) {
        derivedStateOf { book?.chapters?.any { it.isDownloaded } ?: false }
    }


    AppScaffold(
        title = book?.name ?: stringResource(R.string.common_loading),
        onBack = onNavigateBack,
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(visible = isDownloading) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.cancelAllDownloads() },
                        icon = { Icon(Icons.Default.Cancel, stringResource(R.string.common_cancel)) },
                        text = { Text(stringResource(R.string.common_cancel)) },
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                }

                if (!isDownloading) {
                    AnimatedVisibility(visible = hasDownloadableChapters) {
                        ExtendedFloatingActionButton(
                            onClick = { book?.chapters?.let { viewModel.downloadAllChapters(it) } },
                            icon = { Icon(Icons.Default.Download, stringResource(R.string.common_download)) },
                            text = { Text(stringResource(R.string.audiobook_download_all)) }
                        )
                    }
                    AnimatedVisibility(visible = hasDeletableChapters) {
                        ExtendedFloatingActionButton(
                            onClick = { book?.chapters?.let { viewModel.deleteAllDownloadedChapters(it) } },
                            icon = { Icon(Icons.Default.Delete, stringResource(R.string.common_delete)) },
                            text = { Text(stringResource(R.string.audiobook_delete_all_downloads)) },
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        when {
            chapterUiState.isLoading -> AppLoading()
            book != null -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(start = AppPaddings.l, end = AppPaddings.l, top = AppPaddings.l, bottom = 160.dp), // Mărit spațiul de jos
                    verticalArrangement = Arrangement.spacedBy(AppPaddings.m)
                ) {
                    items(book.chapters, key = { it.id }) { chapter ->
                        ChapterItem(
                            chapter = chapter,
                            isCurrentlyPlaying = chapter.id == currentPlayingId,
                            downloadState = mainUiState.downloadStates[chapter.id],
                            progress = mainUiState.downloadProgress[chapter.id] ?: 0,
                            onClick = { onNavigateToPlayer(chapter.id) },
                            onDownload = { viewModel.downloadChapter(chapter) },
                            onDelete = { viewModel.deleteChapter(chapter) }
                        )
                    }
                }
            }
            else -> AppEmpty(
                message = stringResource(R.string.audiobook_book_not_found),
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun ChapterItem(
    chapter: AudiobookEntity,
    isCurrentlyPlaying: Boolean,
    downloadState: WorkInfo.State?,
    progress: Int,
    onClick: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    val cardColors = if (isCurrentlyPlaying) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    } else {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = cardColors
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isCurrentlyPlaying) Icons.Default.GraphicEq else Icons.Default.Headset,
                contentDescription = stringResource(R.string.audiobook_chapter_icon_desc),
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
                    chapter.isDownloaded -> Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = stringResource(R.string.audiobook_downloaded), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.common_delete), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }
                    downloadState == WorkInfo.State.RUNNING -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val animatedProgress by animateFloatAsState(targetValue = progress / 100f, label = "progressAnimation")
                        CircularProgressIndicator(progress = { animatedProgress }, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Text(text = "$progress%", fontSize = 10.sp)
                    }
                    downloadState == WorkInfo.State.ENQUEUED -> Icon(Icons.Default.HourglassTop, contentDescription = stringResource(R.string.audiobook_download_waiting), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    downloadState == WorkInfo.State.FAILED || downloadState == WorkInfo.State.CANCELLED ->
                        IconButton(onClick = onDownload) {
                            Icon(Icons.Default.Refresh, stringResource(R.string.common_retry), tint = MaterialTheme.colorScheme.error)
                        }
                    else ->
                        IconButton(onClick = onDownload) {
                            Icon(Icons.Default.Download, stringResource(R.string.common_download), tint = MaterialTheme.colorScheme.secondary)
                        }
                }
            }
        }
    }
}