package md.ortodox.ortodoxmd.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "settings")

class LanguagePreferences @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        private val LANGUAGE = stringPreferencesKey("language")
    }

    val language: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LANGUAGE] ?: "ro"
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { prefs: Preferences ->
            prefs[LANGUAGE] = lang
        }
    }
}
