package md.ortodox.ortodoxmd.ui.sacrament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.model.Sacrament
import md.ortodox.ortodoxmd.data.repository.SacramentRepository
import javax.inject.Inject

data class SacramentUiState(
    val isLoading: Boolean = true,
    val sacraments: List<Sacrament> = emptyList()
)

@HiltViewModel
class SacramentViewModel @Inject constructor(
    private val repository: SacramentRepository
) : ViewModel() {

    val uiState: StateFlow<SacramentUiState> = repository.getSacraments()
        .map { SacramentUiState(isLoading = false, sacraments = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SacramentUiState()
        )

    init {
        viewModelScope.launch {
            repository.syncSacraments()
        }
    }
}