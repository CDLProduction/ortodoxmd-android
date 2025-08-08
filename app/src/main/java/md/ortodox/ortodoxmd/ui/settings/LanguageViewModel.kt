package md.ortodox.ortodoxmd.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.preferences.LanguagePreferences

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val prefs: LanguagePreferences
) : ViewModel() {
    val language = prefs.language.stateIn(viewModelScope, SharingStarted.Eagerly, "ro")

    fun setLanguage(code: String) {
        viewModelScope.launch { prefs.setLanguage(code) }
    }
}
