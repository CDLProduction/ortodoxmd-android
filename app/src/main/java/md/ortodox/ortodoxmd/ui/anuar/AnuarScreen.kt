package md.ortodox.ortodoxmd.ui.anuar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import md.ortodox.ortodoxmd.data.model.LiturgicalService
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

// Definiții pentru parsarea textului, similare cu cele de la Vieți Sfinți
sealed class ContentBlock {
    data class H2(val text: String) : ContentBlock()
    data class Paragraph(val text: String) : ContentBlock()
}

@Composable
private fun rememberParsedText(rawText: String?): List<ContentBlock> {
    return remember(rawText) {
        if (rawText.isNullOrBlank()) return@remember emptyList()
        rawText.split("\n").filter { it.isNotBlank() }.map { line ->
            // O heuristică simplă: dacă linia conține ':', o considerăm un subtitlu
            if (line.contains(":") && line.length < 100) ContentBlock.H2(line)
            else ContentBlock.Paragraph(line)
        }
    }
}

// Funcție pentru a asocia un tip de slujbă cu un nume și o pictogramă
private fun mapServiceType(type: String): Pair<String, ImageVector> {
    return when (type.lowercase()) {
        "vespers" -> "Vecernie" to Icons.Default.WbTwilight
        "matins" -> "Utrenie" to Icons.Default.WbSunny
        "liturgy" -> "Sfânta Liturghie" to Icons.Default.Church
        else -> type.replaceFirstChar { it.uppercase() } to Icons.Default.MenuBook
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnuarScreen(
    viewModel: AnuarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            DateSelectorBar(
                selectedDate = uiState.selectedDate,
                onPreviousDay = viewModel::goToPreviousDay,
                onNextDay = viewModel::goToNextDay,
                onDateClick = { showDatePicker = true }
            )
        }
    ) { paddingValues ->
        Crossfade(
            targetState = uiState.isLoading,
            label = "ContentFade",
            modifier = Modifier.padding(paddingValues)
        ) { isLoading ->
            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                uiState.services.isEmpty() -> Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Pentru această zi nu sunt disponibile rânduieli.", textAlign = TextAlign.Center)
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.services, key = { it.id }) { service ->
                            ServiceCard(service = service)
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedLocalDate = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        viewModel.selectDate(selectedLocalDate)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Anulează") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DateSelectorBar(
    selectedDate: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onDateClick: () -> Unit
) {
    val dayOfWeek = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("ro")).replaceFirstChar { it.uppercase() }
    val month = selectedDate.month.getDisplayName(TextStyle.FULL, Locale("ro"))
    val formattedDate = "$dayOfWeek, ${selectedDate.dayOfMonth} $month ${selectedDate.year}"

    Surface(tonalElevation = 4.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPreviousDay) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Ziua precedentă")
            }
            TextButton(onClick = onDateClick) {
                Text(formattedDate, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onNextDay) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Ziua următoare")
            }
        }
    }
}

@Composable
private fun ServiceCard(service: LiturgicalService) {
    val (title, icon) = mapServiceType(service.serviceType)
    val parsedDetails = rememberParsedText(service.formattedDetailsRo.ifEmpty { service.formattedDetailsRu })

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleLarge)
            }
            Divider(modifier = Modifier.padding(vertical = 12.dp))

            if (parsedDetails.isEmpty()) {
                Text("Nu sunt detalii pentru această slujbă.", style = MaterialTheme.typography.bodyMedium)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    parsedDetails.forEach { block ->
                        when (block) {
                            is ContentBlock.H2 -> Text(
                                text = block.text,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            )
                            is ContentBlock.Paragraph -> Text(
                                text = block.text,
                                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}