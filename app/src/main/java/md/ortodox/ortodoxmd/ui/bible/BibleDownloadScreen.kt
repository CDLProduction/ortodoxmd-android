package md.ortodox.ortodoxmd.ui.bible

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun BibleDownloadScreen(
    viewModel: BibleDownloadViewModel = hiltViewModel()
) {
    val state by viewModel.downloadState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val currentState = state) {
            is DownloadState.Idle -> {
                Text("Descărcați Sfânta Scriptură pentru acces offline complet, inclusiv căutare și semne de carte.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.startDownload() }) {
                    Text("Descărcare (aprox. 10 MB)")
                }
            }
            is DownloadState.Downloading -> {
                val animatedProgress by animateFloatAsState(targetValue = currentState.progress, label = "progress")
                Text("Descărcare în curs...")
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
                Spacer(Modifier.height(8.dp))
                Text(String.format("%.0f%%", animatedProgress * 100))
            }
            is DownloadState.Success -> {
                Text("Sfânta Scriptură a fost descărcată cu succes!")
                Text("Funcțiile de căutare și semne de carte sunt acum complet funcționale.")
            }
            is DownloadState.Error -> {
                Text("Eroare la descărcare: ${currentState.message}", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.startDownload() }) {
                    Text("Reîncearcă")
                }
            }
        }
    }
}
