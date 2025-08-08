@file:Suppress("OPT_IN_ARGUMENT_IS_NOT_MARKER")

package md.ortodox.ortodoxmd.ui.audiobook

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import md.ortodox.ortodoxmd.R
import java.util.concurrent.TimeUnit

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class, UnstableApi::class)
@Composable
fun AudiobookPlayerScreen(
    navController: NavController,
    viewModel: AudiobookPlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.audiobook?.title ?: stringResource(R.string.common_loading)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Crossfade(targetState = uiState.isReady, label = "PlayerContentFade") { isReady ->
            if (isReady && uiState.audiobook != null) {
                PlayerContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    state = uiState,
                    onPlayPauseToggle = viewModel::onPlayPauseToggle,
                    onSeek = viewModel::onSeek,
                    onRewind = viewModel::onRewind,
                    onForward = viewModel::onForward,
                    onNext = viewModel::onNext,
                    onPrevious = viewModel::onPrevious
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun PlayerContent(
    modifier: Modifier = Modifier,
    state: PlayerUiState,
    onPlayPauseToggle: () -> Unit,
    onSeek: (Long) -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        PlayerHeader(
            title = state.audiobook?.title ?: "",
            author = state.audiobook?.author ?: ""
        )
        PlayerControls(
            isPlaying = state.isPlaying,
            currentPositionMillis = state.currentPositionMillis,
            totalDurationMillis = state.totalDurationMillis,
            onPlayPauseToggle = onPlayPauseToggle,
            onSeek = onSeek,
            onRewind = onRewind,
            onForward = onForward,
            onNext = onNext,
            onPrevious = onPrevious
        )
    }
}

@Composable
private fun PlayerHeader(title: String, author: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(32.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.MenuBook,
            contentDescription = stringResource(R.string.audiobook_player_cover_desc),
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .aspectRatio(1f),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(32.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = author,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    currentPositionMillis: Long,
    totalDurationMillis: Long,
    onPlayPauseToggle: () -> Unit,
    onSeek: (Long) -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    val controlsEnabled = totalDurationMillis > 0

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Slider(
            value = currentPositionMillis.toFloat(),
            onValueChange = { onSeek(it.toLong()) },
            valueRange = 0f..(totalDurationMillis.toFloat().coerceAtLeast(1f)),
            modifier = Modifier.fillMaxWidth(),
            enabled = controlsEnabled
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatDuration(currentPositionMillis), style = MaterialTheme.typography.labelMedium)
            Text(text = formatDuration(totalDurationMillis), style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious, enabled = controlsEnabled) {
                Icon(Icons.Default.SkipPrevious, stringResource(R.string.audiobook_player_previous), modifier = Modifier.size(32.dp))
            }
            IconButton(onClick = onRewind, enabled = controlsEnabled) {
                Icon(Icons.Default.Replay10, stringResource(R.string.audiobook_player_rewind), modifier = Modifier.size(32.dp))
            }
            FilledIconButton(
                onClick = onPlayPauseToggle,
                enabled = controlsEnabled,
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.audiobook_player_play_pause),
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            IconButton(onClick = onForward, enabled = controlsEnabled) {
                Icon(Icons.Default.Forward30, stringResource(R.string.audiobook_player_forward), modifier = Modifier.size(32.dp))
            }
            IconButton(onClick = onNext, enabled = controlsEnabled) {
                Icon(Icons.Default.SkipNext, stringResource(R.string.audiobook_player_next), modifier = Modifier.size(32.dp))
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

private fun formatDuration(millis: Long): String {
    if (millis < 0) return "00:00"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%02d:%02d", minutes, seconds)
}