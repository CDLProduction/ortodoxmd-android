package md.ortodox.ortodoxmd.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity
import md.ortodox.ortodoxmd.data.repository.AudiobookRepository
import md.ortodox.ortodoxmd.data.repository.CalendarRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class ResumePlaybackInfo(
    val audiobook: AudiobookEntity,
    val resumePosition: Long
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val currentDate: String = "",
    val saintOfTheDay: String = "Niciun sfânt important astăzi",
    val fastingInfo: String = "Verificare...",
    val verseOfTheDay: String = "Căci unde sunt doi sau trei, adunaţi în numele Meu, acolo sunt şi Eu în mijlocul lor.",
    val verseReference: String = "Matei 18:20",
    val error: String? = null,
    val resumePlaybackInfo: ResumePlaybackInfo? = null
)

@Suppress("DEPRECATION")
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val audiobookRepository: AudiobookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadHomeScreenData()
    }

    private fun loadHomeScreenData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val calendar = Calendar.getInstance()
            val dateApiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            val dateDisplayFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("ro")).format(calendar.time)

            _uiState.update { it.copy(currentDate = dateDisplayFormat.replaceFirstChar { char -> char.uppercase() }) }

            // Logica pentru reluarea ascultării
            val lastPlaybackFlow = audiobookRepository.getLastPlaybackInfo()
            val allAudiobooksFlow = audiobookRepository.getAudiobooks()

            val resumeInfoFlow = combine(lastPlaybackFlow, allAudiobooksFlow) { lastPlayback, allBooks ->
                if (lastPlayback != null) {
                    val bookToResume = allBooks.find { it.id == lastPlayback.audiobookId }
                    if (bookToResume != null) {
                        return@combine ResumePlaybackInfo(bookToResume, lastPlayback.positionMillis)
                    }
                }
                null
            }

            launch {
                resumeInfoFlow.collect { resumeInfo ->
                    _uiState.update { it.copy(resumePlaybackInfo = resumeInfo) }
                }
            }

            // Logica pentru datele calendaristice
            try {
                val calendarData = calendarRepository.getCalendarData(dateApiFormat) // Presupunând că această metodă există și funcționează
                if (calendarData != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            saintOfTheDay = calendarData.saints.firstOrNull()?.nameAndDescriptionRo ?: calendarData.titlesRo,
                            fastingInfo = calendarData.fastingDescriptionRo,
                            error = null
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Nu s-au putut încărca datele pentru astăzi.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Eroare de conexiune.") }
            }
        }
    }
}
