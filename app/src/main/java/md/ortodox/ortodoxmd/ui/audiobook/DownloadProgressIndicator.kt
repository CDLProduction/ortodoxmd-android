package md.ortodox.ortodoxmd.ui.audiobook

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Simple inline progress for individual items
@Composable
fun CompactDownloadIndicator(
    isDownloading: Boolean,
    progress: Int,
    modifier: Modifier = Modifier
) {
    if (isDownloading && progress > 0) {
        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = modifier
                .height(2.dp)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer
        )
    }
}

