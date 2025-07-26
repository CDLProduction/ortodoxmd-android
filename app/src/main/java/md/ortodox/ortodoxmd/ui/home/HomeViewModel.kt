package md.ortodox.ortodoxmd.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.repository.CalendarRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val currentDate: String = "",
    val saintOfTheDay: String = "Niciun sfânt important astăzi",
    val fastingInfo: String = "Verificare...",
    val verseOfTheDay: String = "Căci unde sunt doi sau trei, adunaţi în numele Meu, acolo sunt şi Eu în mijlocul lor.",
    val verseReference: String = "Matei 18:20",
    val error: String? = null
)

@Suppress("DEPRECATION")
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository
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

            try {
                val calendarData = calendarRepository.getCalendarData(dateApiFormat)
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
