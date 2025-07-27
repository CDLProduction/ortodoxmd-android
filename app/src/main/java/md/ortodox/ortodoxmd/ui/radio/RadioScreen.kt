package md.ortodox.ortodoxmd.ui.radio

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun RadioScreen(
    viewModel: RadioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Lista de posturi de radio
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.stations) { station ->
                RadioStationItem(
                    station = station,
                    isSelected = station == uiState.currentStation,
                    onStationClick = { viewModel.onStationSelected(station) }
                )
            }
        }

        // Panoul de control al player-ului
        if (uiState.currentStation != null) {
            PlayerControls(
                station = uiState.currentStation!!,
                isPlaying = uiState.isPlaying,
                isBuffering = uiState.isBuffering, // NOU: Pasăm starea de buffering
                onPlayPauseClick = { viewModel.togglePlayback() }
            )
        }
    }
}

@Composable
fun RadioStationItem(
    station: RadioStation,
    isSelected: Boolean,
    onStationClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onStationClick),
        colors = if (isSelected) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        else CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.Radio, contentDescription = null)
            Text(station.name, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun PlayerControls(
    station: RadioStation,
    isPlaying: Boolean,
    isBuffering: Boolean, // NOU: Primim starea de buffering
    onPlayPauseClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when {
                        isBuffering -> "CONECTARE..."
                        isPlaying -> "ACUM RULEAZĂ"
                        else -> "OPRIT"
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

            // NOU: Afișăm un indicator de progres în timpul buffering-ului
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
                            contentDescription = "Play/Pause",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
