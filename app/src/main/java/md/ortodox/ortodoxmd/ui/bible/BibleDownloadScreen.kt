package md.ortodox.ortodoxmd.ui.bible

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun BibleDownloadScreen(
    viewModel: BibleDownloadViewModel = hiltViewModel(),
    onDownloadComplete: () -> Unit
) {
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudDownload,
            contentDescription = "Descarcă Biblia",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Descărcați Sfânta Scriptură",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Pentru a accesa funcționalitatea completă, este necesară descărcarea textului integral al Bibliei (aprox. 10MB).",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        when (val state = downloadState) {
            is DownloadState.Idle -> {
                Button(onClick = { viewModel.startDownload() }) {
                    Text("Pornește Descărcarea")
                }
            }
            is DownloadState.Downloading -> {
                val animatedProgress by animateFloatAsState(targetValue = state.progress, label = "downloadProgress")
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(text = "Descărcare... ${(animatedProgress * 100).toInt()}%")
                }
            }
            is DownloadState.Error -> {
                Text(state.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.startDownload() }) {
                    Text("Reîncearcă")
                }
            }
            is DownloadState.Finished -> {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Finalizat",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(state.message, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onDownloadComplete) {
                    Text("Accesează Biblia")
                }
            }
        }
    }
}