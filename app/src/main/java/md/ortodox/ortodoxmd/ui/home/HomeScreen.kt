package md.ortodox.ortodoxmd.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header ---
        item {
            HeaderSection(currentDate = uiState.currentDate)
        }

        // --- Loading sau Error State ---
        if (uiState.isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (uiState.error != null) {
            item {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }

        // --- Carduri cu Informații ---
        if (!uiState.isLoading && uiState.error == null) {
            item {
                InfoCard(
                    title = "Sfântul Zilei",
                    content = uiState.saintOfTheDay,
                    icon = Icons.Default.Person,
                    color = MaterialTheme.colorScheme.primaryContainer
                )
            }
            item {
                // *** CORECȚIE TEMPORARĂ DUBLĂ APLICATĂ AICI ***
                val correctedFastingInfo = when {
                    uiState.fastingInfo.equals("Harti", ignoreCase = true) -> "Zi fără post"
                    uiState.fastingInfo.equals("Post", ignoreCase = true) -> "Zi de post"
                    else -> uiState.fastingInfo
                }

                InfoCard(
                    title = "Postul Zilei",
                    content = correctedFastingInfo, // Folosim textul corectat
                    icon = Icons.Default.Restaurant,
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
            }
        }

        // --- Versetul Zilei ---
        item {
            VerseOfTheDayCard(
                verse = uiState.verseOfTheDay,
                reference = uiState.verseReference
            )
        }

        // --- Navigare Rapidă ---
        item {
            NavigationSection(navController = navController)
        }
    }
}

@Composable
private fun HeaderSection(currentDate: String) {
    Column {
        Text(
            text = "Bună ziua,",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = currentDate,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun InfoCard(title: String, content: String, icon: ImageVector, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(text = content, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun VerseOfTheDayCard(verse: String, reference: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "\"${verse}\"",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic
            )
            Spacer(modifier = Modifier.height(8.dp))
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
private fun NavigationSection(navController: NavHostController) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Explorați Aplicația",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NavigationCard(
                title = "Calendar",
                icon = Icons.Default.CalendarMonth,
                onClick = { navController.navigate("calendar") },
                modifier = Modifier.weight(1f)
            )
            NavigationCard(
                title = "Rugăciuni",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                onClick = { navController.navigate("prayer_categories") },
                modifier = Modifier.weight(1f)
            )
        }
        NavigationCard(
            title = "Sfânta Scriptură",
            icon = Icons.Default.Book,
            onClick = { navController.navigate("bible_home") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavigationCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(32.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium)
        }
    }
}
