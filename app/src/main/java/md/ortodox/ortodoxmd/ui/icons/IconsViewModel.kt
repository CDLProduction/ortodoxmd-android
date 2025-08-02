package md.ortodox.ortodoxmd.ui.icons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.repository.IconRepository
import md.ortodox.ortodoxmd.data.model.Icon
import javax.inject.Inject

@HiltViewModel
class IconsViewModel @Inject constructor(
    private val repository: IconRepository
) : ViewModel() {
    val icons: StateFlow<List<Icon>> = repository.getIcons()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.syncIcons()
        }
    }
}