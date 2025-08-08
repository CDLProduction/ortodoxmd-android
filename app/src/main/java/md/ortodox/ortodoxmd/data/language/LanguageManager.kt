package md.ortodox.ortodoxmd.data.language

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class LanguageManager @Inject constructor(@ApplicationContext private val context: Context) {

    // Folosim SharedPreferences, care este sincron și sigur pentru a fi folosit
    // la pornirea aplicației.
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

    companion object {
        const val LANGUAGE_KEY = "app_language"
        const val DEFAULT_LANGUAGE = "ro" // Limba implicită
    }

    // Un Flow simplu care emite limba salvată.
    val currentLanguage: Flow<String> = flow {
        emit(prefs.getString(LANGUAGE_KEY, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE)
    }

    // Funcția pentru setarea limbii. Nu mai este 'suspend'.
    fun setLanguage(langCode: String) {
        // Metoda oficială pentru a seta limba la nivel de aplicație.
        val localeList = LocaleListCompat.forLanguageTags(langCode)
        AppCompatDelegate.setApplicationLocales(localeList)

        // Salvăm preferința pentru viitoarele porniri.
        prefs.edit { putString(LANGUAGE_KEY, langCode) }
    }

    // Funcția sincronă pentru a citi limba.
    // Aceasta este esențială pentru a fi apelată în OrtodoxMDApplication.onCreate.
    fun getCurrentLanguageSync(): String {
        return prefs.getString(LANGUAGE_KEY, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }
}
