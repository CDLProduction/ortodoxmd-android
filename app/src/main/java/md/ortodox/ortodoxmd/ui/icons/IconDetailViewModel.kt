package md.ortodox.ortodoxmd.ui.icons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.model.Icon
import md.ortodox.ortodoxmd.data.repository.IconRepository
import javax.inject.Inject

@HiltViewModel
class IconDetailViewModel @Inject constructor(
    private val repository: IconRepository
) : ViewModel() {
    private val _icon = MutableStateFlow<Icon?>(null)
    val icon = _icon.asStateFlow()

    fun loadIcon(id: Long) {
        viewModelScope.launch {
            _icon.value = repository.getById(id)
        }
    }
}