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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.data.model.LiturgicalService
import md.ortodox.ortodoxmd.ui.design.AppCard
import md.ortodox.ortodoxmd.ui.design.AppEmpty
import md.ortodox.ortodoxmd.ui.design.AppLoading
import md.ortodox.ortodoxmd.ui.design.AppPaddings
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

// Definițiile pentru parsarea textului rămân neschimbate
sealed class ContentBlock {
    data class H2(val text: String) : ContentBlock()
    data class Paragraph(val text: String) : ContentBlock()
}

@Composable
private fun rememberParsedText(rawText: String?): List<ContentBlock> {
    return remember(rawText) {
        if (rawText.isNullOrBlank()) return@remember emptyList()
        rawText.split("\n").filter { it.isNotBlank() }.map { line ->
            if (line.contains(":") && line.length < 100) ContentBlock.H2(line)
            else ContentBlock.Paragraph(line)
        }
    }
}

// Funcția pentru maparea serviciului rămâne neschimbată
@Composable
private fun mapServiceType(type: String): Pair<String, ImageVector> {
    return when (type.lowercase()) {
        "vespers" -> stringResource(R.string.service_type_vespers) to Icons.Default.WbTwilight
        "matins" -> stringResource(R.string.service_type_matins) to Icons.Default.WbSunny
        "liturgy" -> stringResource(R.string.service_type_liturgy) to Icons.Default.Church
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

    // Structura principală cu Scaffold rămâne, deoarece are un TopBar custom
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
                // REFACTORIZAT: Folosim componenta AppLoading
                isLoading -> AppLoading()

                // REFACTORIZAT: Folosim componenta AppEmpty
                uiState.services.isEmpty() -> AppEmpty(message = stringResource(R.string.anuar_no_services_for_day))

                else -> {
                    LazyColumn(
                        // REFACTORIZAT: Folosim AppPaddings pentru consistență
                        contentPadding = AppPaddings.content,
                        verticalArrangement = Arrangement.spacedBy(AppPaddings.l) // Spațiere consistentă
                    ) {
                        items(uiState.services, key = { it.id }) { service ->
                            ServiceCard(service = service)
                        }
                    }
                }
            }
        }
    }

    // Logica pentru DatePickerDialog rămâne neschimbată
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
                }) { Text(stringResource(R.string.anuar_ok_button)) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.anuar_cancel_button)) } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// DateSelectorBar este o componentă specifică acestui ecran și rămâne neschimbată
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
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, stringResource(R.string.anuar_previous_day))
            }
            TextButton(onClick = onDateClick) {
                Text(formattedDate, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onNextDay) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, stringResource(R.string.anuar_next_day))
            }
        }
    }
}

@Composable
private fun ServiceCard(service: LiturgicalService) {
    val (title, icon) = mapServiceType(service.serviceType)
    val parsedDetails = rememberParsedText(service.formattedDetailsRo.ifEmpty { service.formattedDetailsRu })

    // REFACTORIZAT: Folosim componenta AppCard
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AppPaddings.l)) { // Spațiere consistentă
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(AppPaddings.m))
                Text(title, style = MaterialTheme.typography.titleLarge)
            }
            Divider(modifier = Modifier.padding(vertical = AppPaddings.m))

            if (parsedDetails.isEmpty()) {
                Text(stringResource(R.string.anuar_no_details_for_service), style = MaterialTheme.typography.bodyMedium)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(AppPaddings.s)) {
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
