package md.ortodox.ortodoxmd.ui.saints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.model.SaintLife
import md.ortodox.ortodoxmd.data.repository.SaintLifeRepository
import javax.inject.Inject

@HiltViewModel
class SaintLifeDetailViewModel @Inject constructor(
    private val repository: SaintLifeRepository
) : ViewModel() {
    private val _saintLife = MutableStateFlow<SaintLife?>(null)
    val saintLife = _saintLife.asStateFlow()

    fun loadSaintLife(id: Long) {
        viewModelScope.launch {
            _saintLife.value = repository.getById(id)
        }
    }
}