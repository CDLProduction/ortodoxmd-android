//package md.ortodox.ortodoxmd.ui.audiobook
//
//import androidx.compose.animation.Crossfade
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.CheckCircle
//import androidx.compose.material.icons.filled.Download
//import androidx.compose.material.icons.filled.Headset
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.navigation.NavController
//import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity
//
//@Composable
//fun AudiobooksScreen(
//    navController: NavController,
//    viewModel: AudiobookViewModel = hiltViewModel()
//) {
//    val uiState by viewModel.uiState.collectAsState()
//
//    Crossfade(targetState = uiState.isLoading, label = "ListLoadingFade") { isLoading ->
//        if (isLoading) {
//            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                CircularProgressIndicator()
//            }
//        } else {
//            AudiobookList(
//                audiobooks = uiState.audiobooks,
//                onAudiobookClick = { book ->
//                    // CORECȚIE: Se navighează întotdeauna la player, indiferent de starea descărcării.
//                    navController.navigate("audiobook_player/${book.id}")
//                },
//                onDownloadClick = { book ->
//                    viewModel.downloadAudiobook(book)
//                }
//            )
//        }
//    }
//}
//
//@Composable
//private fun AudiobookList(
//    audiobooks: List<AudiobookEntity>,
//    onAudiobookClick: (AudiobookEntity) -> Unit,
//    onDownloadClick: (AudiobookEntity) -> Unit
//) {
//    LazyColumn(
//        contentPadding = PaddingValues(16.dp),
//        verticalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//        items(audiobooks, key = { it.id }) { audiobook ->
//            AudiobookItem(
//                audiobook = audiobook,
//                onClick = { onAudiobookClick(audiobook) },
//                onDownload = { onDownloadClick(audiobook) }
//            )
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun AudiobookItem(
//    audiobook: AudiobookEntity,
//    onClick: () -> Unit,
//    onDownload: () -> Unit
//) {
//    // Păstrăm distincția vizuală, dar cardul va fi mereu activabil.
//    val cardAlpha = if (audiobook.isDownloaded) 1f else 0.7f
//
//    Card(
//        onClick = onClick,
//        enabled = true, // CORECȚIE: Cardul este întotdeauna activabil.
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = cardAlpha)
//        )
//    ) {
//        Row(
//            modifier = Modifier.padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Icon(
//                imageVector = Icons.Default.Headset,
//                contentDescription = "Icoană Carte Audio",
//                modifier = Modifier.size(40.dp),
//                tint = MaterialTheme.colorScheme.primary
//            )
//
//            Spacer(Modifier.width(16.dp))
//
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    text = audiobook.title,
//                    style = MaterialTheme.typography.titleMedium,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//                Text(
//                    text = audiobook.author,
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//
//            Spacer(Modifier.width(16.dp))
//
//            if (audiobook.isDownloaded) {
//                Icon(
//                    imageVector = Icons.Default.CheckCircle,
//                    contentDescription = "Descărcat",
//                    tint = MaterialTheme.colorScheme.primary
//                )
//            } else {
//                IconButton(onClick = onDownload) {
//                    Icon(
//                        imageVector = Icons.Default.Download,
//                        contentDescription = "Descarcă",
//                        tint = MaterialTheme.colorScheme.secondary
//                    )
//                }
//            }
//        }
//    }
//}
