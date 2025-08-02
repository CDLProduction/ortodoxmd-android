package md.ortodox.ortodoxmd.ui.anuar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.model.LiturgicalService
import md.ortodox.ortodoxmd.data.repository.LiturgicalRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class AnuarUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val services: List<LiturgicalService> = emptyList(),
    val isLoading: Boolean = true
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class AnuarViewModel @Inject constructor(
    private val repository: LiturgicalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnuarUiState())
    val uiState: StateFlow<AnuarUiState> = _uiState.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private var dataFetchJob: Job? = null

    init {
        // La pornire, încarcă datele pentru ziua curentă
        selectDate(LocalDate.now())
    }

    fun selectDate(date: LocalDate) {
        // Anulează jobul anterior pentru a nu avea actualizări concurente
        dataFetchJob?.cancel()

        // Actualizează imediat data selectată și setează starea de încărcare
        _uiState.update { it.copy(isLoading = true, selectedDate = date) }

        dataFetchJob = viewModelScope.launch {
            // Pasul 1: Sincronizează datele de la server pentru noua dată
            repository.syncServicesForDate(date.format(dateFormatter))

            // Pasul 2: Colectează datele actualizate din baza de date locală
            // Colectarea va continua până la anularea jobului
            repository.getServicesByDate(date.format(dateFormatter))
                .collect { services ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            services = services,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun goToNextDay() {
        selectDate(_uiState.value.selectedDate.plusDays(1))
    }

    fun goToPreviousDay() {
        selectDate(_uiState.value.selectedDate.minusDays(1))
    }
}