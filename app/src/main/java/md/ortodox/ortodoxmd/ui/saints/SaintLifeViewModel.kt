package md.ortodox.ortodoxmd.ui.saints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.model.SaintLife
import md.ortodox.ortodoxmd.data.repository.SaintLifeRepository
import javax.inject.Inject

@HiltViewModel
class SaintLifeViewModel @Inject constructor(
    private val repository: SaintLifeRepository
) : ViewModel() {
    val saintLives: StateFlow<List<SaintLife>> = repository.getSaintLives()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.syncSaintLives()
        }
    }
}