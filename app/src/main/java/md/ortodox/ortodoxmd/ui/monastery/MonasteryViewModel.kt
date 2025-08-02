package md.ortodox.ortodoxmd.ui.monastery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.model.Monastery
import md.ortodox.ortodoxmd.data.repository.MonasteryRepository
import javax.inject.Inject

data class MonasteryUiState(
    val isLoading: Boolean = true,
    val monasteries: List<Monastery> = emptyList()
)

@HiltViewModel
class MonasteryViewModel @Inject constructor(
    private val repository: MonasteryRepository
) : ViewModel() {

    val uiState: StateFlow<MonasteryUiState> = repository.getMonasteries()
        .map { MonasteryUiState(isLoading = false, monasteries = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MonasteryUiState()
        )

    init {
        viewModelScope.launch {
            repository.syncMonasteries()
        }
    }
}