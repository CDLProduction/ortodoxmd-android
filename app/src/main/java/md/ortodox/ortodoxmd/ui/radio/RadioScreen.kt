package md.ortodox.ortodoxmd.ui.radio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.ui.design.AppListItem
import md.ortodox.ortodoxmd.ui.design.AppPaddings
import md.ortodox.ortodoxmd.ui.design.AppScaffold

@Composable
fun RadioScreen(
    viewModel: RadioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // REFACTORIZAT: Folosim AppScaffold.
    AppScaffold(title = stringResource(id = R.string.menu_radio)) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = AppPaddings.content,
                verticalArrangement = Arrangement.spacedBy(AppPaddings.s)
            ) {
                items(uiState.stations) { station ->
                    // REFACTORIZAT: Folosim AppListItem pentru un aspect consistent.
                    AppListItem(
                        title = station.name,
                        leading = {
                            Icon(
                                Icons.Default.Radio,
                                contentDescription = stringResource(R.string.radio_station_icon_desc),
                                tint = if (station == uiState.currentStation) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        },
                        onClick = { viewModel.onStationSelected(station) }
                    )
                }
            }

            // Bara de control a player-ului este o componentă specifică și rămâne neschimbată.
            if (uiState.currentStation != null) {
                PlayerControls(
                    station = uiState.currentStation!!,
                    isPlaying = uiState.isPlaying,
                    isBuffering = uiState.isBuffering,
                    onPlayPauseClick = { viewModel.togglePlayback() }
                )
            }
        }
    }
}

// Această componentă este specifică acestui ecran și rămâne neschimbată.
@Composable
fun PlayerControls(
    station: RadioStation,
    isPlaying: Boolean,
    isBuffering: Boolean,
    onPlayPauseClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .padding(AppPaddings.l)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppPaddings.l)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when {
                        isBuffering -> stringResource(R.string.radio_status_connecting)
                        isPlaying -> stringResource(R.string.radio_status_playing)
                        else -> stringResource(R.string.radio_status_stopped)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isBuffering) {
                    CircularProgressIndicator()
                } else {
                    IconButton(
                        onClick = onPlayPauseClick,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.radio_play_pause_desc),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
