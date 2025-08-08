package md.ortodox.ortodoxmd.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.model.CalendarData
import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity
import md.ortodox.ortodoxmd.data.repository.AudiobookRepository
import md.ortodox.ortodoxmd.data.repository.CalendarRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ResumePlaybackInfo(
    val audiobook: AudiobookEntity,
    val resumePosition: Long
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val calendarData: CalendarData? = null,
    val verseOfTheDay: String = "",
    val verseReference: String = "",
    val error: String? = null,
    val resumePlaybackInfo: ResumePlaybackInfo? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val audiobookRepository: AudiobookRepository
    // Am eliminat BibleRepository de aici
) : ViewModel(), DefaultLifecycleObserver {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    // Datele se vor reîncărca de fiecare dată când ecranul devine activ
    override fun onStart(owner: LifecycleOwner) {
        loadHomeScreenData()
    }

    private fun loadHomeScreenData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // --- Logica pentru Versetul Zilei (acum doar statică) ---
            val staticVerse = StaticVerseProvider.getRandomVerse()
            _uiState.update {
                it.copy(
                    verseOfTheDay = staticVerse.text,
                    verseReference = staticVerse.reference
                )
            }

            // --- Logica pentru datele calendaristice ---
            val dateApiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            try {
                val calendarData = calendarRepository.getCalendarData(dateApiFormat)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        calendarData = calendarData,
                        error = if (calendarData == null) "Nu s-au putut încărca datele pentru astăzi." else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Eroare de conexiune.") }
            }

            // --- Logica pentru reluarea ascultării ---
            val lastPlayback = audiobookRepository.getLastPlaybackInfo().first()
            val bookToResume = lastPlayback?.let { audiobookRepository.getById(it.audiobookId) }
            _uiState.update {
                it.copy(
                    resumePlaybackInfo = if (bookToResume != null && lastPlayback != null) {
                        ResumePlaybackInfo(bookToResume, lastPlayback.positionMillis)
                    } else {
                        null
                    }
                )
            }
        }
    }
}
