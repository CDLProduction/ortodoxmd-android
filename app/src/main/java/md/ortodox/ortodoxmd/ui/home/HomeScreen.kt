package md.ortodox.ortodoxmd.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- Cardul Zilei ---
            TodayCard(
                calendarData = uiState.calendarData,
                onClick = { navController.navigate("calendar") }
            )

            // --- Cardul Versetul Zilei ---
            VerseOfTheDayCard(
                verse = uiState.verseOfTheDay,
                reference = uiState.verseReference
            )

            // --- Card Reluare Ascultare (apare doar dacă există) ---
            uiState.resumePlaybackInfo?.let { info ->
                ResumeListeningCard(
                    info = info,
                    onClick = { navController.navigate("audiobook_player/${info.audiobook.id}") }
                )
            }

            // --- Grila de Navigare ---
            QuickNavGrid(navController = navController)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayCard(calendarData: md.ortodox.ortodoxmd.data.model.CalendarData?, onClick: () -> Unit) {
    val calendar = Calendar.getInstance()
    val dateDisplayFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("ro")).format(calendar.time)
    val title = dateDisplayFormat.replaceFirstChar { it.uppercase() }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Divider()
            if (calendarData != null) {
                val fastingInfo = when (calendarData.fastingDescriptionRo.lowercase(Locale.ROOT)) {
                    "harti" -> "Zi fără post"
                    else -> calendarData.fastingDescriptionRo
                }
                InfoRow(icon = Icons.Default.Restaurant, text = fastingInfo)
                InfoRow(icon = Icons.Default.Person, text = calendarData.saints.firstOrNull()?.nameAndDescriptionRo ?: calendarData.summaryTitleRo)
            } else {
                InfoRow(icon = Icons.Default.CloudOff, text = "Datele nu au putut fi încărcate.")
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(20.dp)
        )
        Text(text = text, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun VerseOfTheDayCard(verse: String, reference: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "\"$verse\"",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = reference,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResumeListeningCard(info: ResumePlaybackInfo, onClick: () -> Unit) {
    // ... conținutul acestei funcții rămâne neschimbat ...
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PlayCircleFilled,
                contentDescription = "Reluați Ascultarea",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Reluați Ascultarea",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = info.audiobook.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val formattedDuration = remember(info.resumePosition) {
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(info.resumePosition)
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(info.resumePosition) - TimeUnit.MINUTES.toSeconds(minutes)
                    String.format("%02d:%02d", minutes, seconds)
                }
                Text(
                    text = "Rămas la: $formattedDuration",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun QuickNavGrid(navController: NavHostController) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Explorați Aplicația",
            style = MaterialTheme.typography.titleLarge
        )
        // Primul rând de butoane
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            NavButton(
                title = "Biblia",
                icon = Icons.Default.Book,
                onClick = { navController.navigate("bible_home") },
                modifier = Modifier.weight(1f)
            )
            NavButton(
                title = "Rugăciuni",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                onClick = { navController.navigate("prayer_categories") },
                modifier = Modifier.weight(1f)
            )
        }
        // Al doilea rând de butoane
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            NavButton(
                title = "Vieți Sfinți",
                icon = Icons.Default.Person,
                onClick = { navController.navigate("saint_lives") },
                modifier = Modifier.weight(1f)
            )
            NavButton(
                title = "Anuar",
                icon = Icons.Default.Today,
                onClick = { navController.navigate("anuar") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(28.dp))
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}