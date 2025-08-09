package md.ortodox.ortodoxmd.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.ui.design.AppCard
import md.ortodox.ortodoxmd.ui.design.AppLoading
import md.ortodox.ortodoxmd.ui.design.AppPaddings
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(viewModel)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(viewModel)
        }
    }

    if (uiState.isLoading) {
        // REFACTORIZAT: Folosim AppLoading.
        AppLoading()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppPaddings.l),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            TodayCard(
                calendarData = uiState.calendarData,
                onClick = { navController.navigate("calendar") }
            )
            VerseOfTheDayCard(
                verse = uiState.verseOfTheDay,
                reference = uiState.verseReference
            )
            uiState.resumePlaybackInfo?.let { info ->
                ResumeListeningCard(
                    info = info,
                    onClick = { navController.navigate("audiobook_player/${info.audiobook.id}") }
                )
            }
            QuickNavGrid(navController = navController)
        }
    }
}

@Composable
private fun TodayCard(calendarData: md.ortodox.ortodoxmd.data.model.CalendarData?, onClick: () -> Unit) {
    val dateDisplayFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("ro")).format(Date())
    val title = dateDisplayFormat.replaceFirstChar { it.uppercase() }

    // REFACTORIZAT: Folosim AppCard.
    AppCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(AppPaddings.l),
            verticalArrangement = Arrangement.spacedBy(AppPaddings.m)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Divider()
            if (calendarData != null) {
                val fastingInfo = when (calendarData.fastingDescriptionRo.lowercase(Locale.ROOT)) {
                    "harti" -> stringResource(R.string.home_no_fasting_day)
                    "post" -> stringResource(R.string.home_fasting_day)
                    else -> calendarData.fastingDescriptionRo
                }
                InfoRow(icon = Icons.Default.Restaurant, text = fastingInfo)
                InfoRow(
                    icon = Icons.Default.Person,
                    text = calendarData.saints.firstOrNull()?.nameAndDescriptionRo ?: calendarData.summaryTitleRo
                )
            } else {
                InfoRow(icon = Icons.Default.CloudOff, text = stringResource(R.string.home_data_load_error))
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppPaddings.m)
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
    // REFACTORIZAT: Folosim AppCard.
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(AppPaddings.l)) {
            Text(
                text = "\"$verse\"",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(AppPaddings.s))
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

@Composable
private fun ResumeListeningCard(info: ResumePlaybackInfo, onClick: () -> Unit) {
    // REFACTORIZAT: Folosim AppCard.
    AppCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(AppPaddings.l),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PlayCircleFilled,
                contentDescription = stringResource(R.string.home_resume_listening),
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.tertiary // Culoarea se potrivește bine cu designul original
            )
            Spacer(Modifier.width(AppPaddings.l))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_resume_listening),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface // Culoare standard pentru text
                )
                Text(
                    text = info.audiobook.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val formattedDuration = remember(info.resumePosition) {
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(info.resumePosition)
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(info.resumePosition) - TimeUnit.MINUTES.toSeconds(minutes)
                    String.format("%02d:%02d", minutes, seconds)
                }
                Text(
                    text = stringResource(R.string.home_resume_at, formattedDuration),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun QuickNavGrid(navController: NavHostController) {
    Column(verticalArrangement = Arrangement.spacedBy(AppPaddings.m)) {
        Text(
            text = stringResource(R.string.home_explore_app),
            style = MaterialTheme.typography.titleLarge
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppPaddings.l)) {
            NavButton(
                title = stringResource(R.string.home_bible),
                icon = Icons.Default.Book,
                onClick = { navController.navigate("bible_home") },
                modifier = Modifier.weight(1f)
            )
            NavButton(
                title = stringResource(R.string.home_prayers),
                icon = Icons.AutoMirrored.Filled.MenuBook,
                onClick = { navController.navigate("prayer_categories") },
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppPaddings.l)) {
            NavButton(
                title = stringResource(R.string.home_saints_lives),
                icon = Icons.Default.Person,
                onClick = { navController.navigate("saint_lives") },
                modifier = Modifier.weight(1f)
            )
            NavButton(
                title = stringResource(R.string.home_anuar),
                icon = Icons.Default.Today,
                onClick = { navController.navigate("anuar") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NavButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // REFACTORIZAT: Folosim AppCard pentru butoanele de navigare.
    AppCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = AppPaddings.l, horizontal = AppPaddings.s),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppPaddings.s)
        ) {
            Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(28.dp))
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}
