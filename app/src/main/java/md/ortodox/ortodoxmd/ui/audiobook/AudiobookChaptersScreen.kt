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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
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
    navController: NavController,
    viewModel: AudiobookViewModel,
    mainViewModel: MainViewModel,
    onNavigateToPlayer: (Long) -> Unit
) {
    val miniPlayerState by mainViewModel.miniPlayerState.collectAsStateWithLifecycle()
    val currentPlayingId = miniPlayerState.currentTrackId

    val chapterUiState by viewModel.selectedBookState.collectAsStateWithLifecycle()
    val mainUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDownloading by viewModel.isDownloading.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose { viewModel.clearBookSelection() }
    }

    val book = chapterUiState.book

    // Cache expensive derivations with proper keys
    val hasDownloadableChapters by remember(book?.chapters) {
        derivedStateOf { 
            book?.chapters?.any { !it.isDownloaded } ?: false 
        }
    }
    
    val hasDeletableChapters by remember(book?.chapters, mainUiState.downloadStates) {
        derivedStateOf { 
            book?.chapters?.any { chapter ->
                chapter.isDownloaded || mainUiState.downloadStates[chapter.id] == WorkInfo.State.SUCCEEDED
            } ?: false 
        }
    }
    
    // Cache download state lookups for better performance
    val downloadStatesCache by remember(mainUiState.downloadStates, mainUiState.downloadProgress) {
        derivedStateOf {
            book?.chapters?.associate { chapter ->
                chapter.id to DownloadStateInfo(
                    state = mainUiState.downloadStates[chapter.id],
                    progress = mainUiState.downloadProgress[chapter.id] ?: 0
                )
            } ?: emptyMap()
        }
    }

    AppScaffold(
        title = book?.name ?: stringResource(R.string.common_loading),
        onBack = { navController.popBackStack() },
        floatingActionButton = {
            // --- BUTONUL PENTRU "DESCĂRCĂRI" A FOST ELIMINAT DE AICI ---
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
                            onClick = { book?.chapters?.let { viewModel.downloadAllChapters(it.toList()) } },
                            icon = { Icon(Icons.Default.Download, stringResource(R.string.common_download)) },
                            text = { Text(stringResource(R.string.audiobook_download_all)) }
                        )
                    }
                    AnimatedVisibility(visible = hasDeletableChapters) {
                        ExtendedFloatingActionButton(
                            onClick = { book?.chapters?.let { viewModel.deleteAllDownloadedChapters(it.toList()) } },
                            icon = { Icon(Icons.Default.Delete, stringResource(R.string.common_delete)) },
                            text = { Text(stringResource(R.string.audiobook_delete_all_downloads)) },
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            
            
            when {
                chapterUiState.isLoading -> AppLoading(Modifier.fillMaxSize())
                book != null -> {
                    LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = AppPaddings.l, end = AppPaddings.l, top = AppPaddings.l, bottom = 180.dp),
                    verticalArrangement = Arrangement.spacedBy(AppPaddings.m)
                ) {
                    // Use paginated chapters for better performance
                    val chaptersToShow = chapterUiState.paginatedChapters?.chapters ?: book.chapters
                    
                    items(
                        items = chaptersToShow,
                        key = { chapter -> chapter.id },
                        contentType = { chapter -> 
                            when {
                                chapter.isDownloaded -> "downloaded"
                                downloadStatesCache[chapter.id]?.state == WorkInfo.State.RUNNING -> "downloading"
                                downloadStatesCache[chapter.id]?.state == WorkInfo.State.ENQUEUED -> "enqueued"
                                else -> "available"
                            }
                        }
                    ) { chapter ->
                        val downloadStateInfo = downloadStatesCache[chapter.id] 
                        ChapterItem(
                            chapter = chapter,
                            isCurrentlyPlaying = chapter.id == currentPlayingId,
                            downloadState = downloadStateInfo?.state,
                            progress = downloadStateInfo?.progress ?: 0,
                            onClick = { onNavigateToPlayer(chapter.id) },
                            onDownload = { viewModel.downloadChapter(chapter) },
                            onDelete = { viewModel.deleteChapter(chapter) }
                        )
                    }
                    
                    // Add lazy loading footer if needed
                    chapterUiState.paginatedChapters?.let { paginatedData ->
                        if (paginatedData.hasMorePages) {
                            item(
                                key = "load_more_footer",
                                contentType = "load_more"
                            ) {
                                LazyLoadingFooter(
                                    isLoading = chapterUiState.lazyLoadingState.isLoadingMore,
                                    onLoadMore = { viewModel.loadMoreChapters() },
                                    error = chapterUiState.lazyLoadingState.error
                                )
                            }
                        }
                    }
                }
            }
            else -> AppEmpty(
                message = stringResource(R.string.audiobook_book_not_found),
                modifier = Modifier.fillMaxSize()
            )
        }
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
    // Cache expensive computations
    val cardColors = if (isCurrentlyPlaying) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    } else {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    }

    val leadingIcon = if (isCurrentlyPlaying) Icons.Default.GraphicEq else Icons.Default.Headset

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = cardColors
    ) {
        Column {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = stringResource(R.string.audiobook_chapter_icon_desc),
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(16.dp))
                
                // Simplified title section
                Text(
                    text = chapter.displayTitle.ifEmpty { chapter.title },
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Optimized action section - simplified to just icons
                ChapterActionSection(
                    isDownloaded = chapter.isDownloaded,
                    downloadState = downloadState,
                    progress = progress,
                    onDownload = onDownload,
                    onDelete = onDelete
                )
            }
            
            // Compact download indicator at the bottom
            CompactDownloadIndicator(
                isDownloading = downloadState == WorkInfo.State.RUNNING,
                progress = progress,
                modifier = Modifier.padding(start = 16.dp, end = 8.dp, bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun ChapterActionSection(
    isDownloaded: Boolean,
    downloadState: WorkInfo.State?,
    progress: Int,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    // Simplified to just action buttons - no heavy progress circles
    when {
        isDownloaded -> {
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.common_delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        downloadState == WorkInfo.State.RUNNING -> {
            Icon(
                Icons.Default.Download,
                contentDescription = "Descărcare în curs",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        downloadState == WorkInfo.State.ENQUEUED -> {
            Icon(
                Icons.Default.HourglassTop,
                contentDescription = "În așteptare",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        downloadState == WorkInfo.State.FAILED || downloadState == WorkInfo.State.CANCELLED -> {
            IconButton(onClick = onDownload) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.common_retry),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        else -> {
            IconButton(onClick = onDownload) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = stringResource(R.string.common_download),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


@Composable
private fun LazyLoadingFooter(
    isLoading: Boolean,
    onLoadMore: () -> Unit,
    error: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppPaddings.l),
        contentAlignment = Alignment.Center
    ) {
        when {
            error != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onLoadMore) {
                        Text(stringResource(R.string.common_retry))
                    }
                }
            }
            isLoading -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = stringResource(R.string.common_loading),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            else -> {
                Button(onClick = onLoadMore) {
                    Text("Load More") // Using hardcoded string as resource doesn't exist
                }
            }
        }
    }
}