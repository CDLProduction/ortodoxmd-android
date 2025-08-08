package md.ortodox.ortodoxmd.ui.language

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import md.ortodox.ortodoxmd.MainActivity
import md.ortodox.ortodoxmd.R

data class Language(val code: String, val nameResId: Int)

private val supportedLanguages = listOf(
    Language("ro", R.string.language_ro),
    Language("en", R.string.language_en),
    Language("ru", R.string.language_ru)
)

@Composable
fun LanguageScreen(
    viewModel: LanguageViewModel = hiltViewModel()
) {
    val currentLanguageCode by viewModel.currentLanguage.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(supportedLanguages) { language ->
            LanguageItem(
                language = language,
                isSelected = language.code == currentLanguageCode,
                onSelect = {
                    // Verificăm dacă limba selectată este diferită de cea curentă
                    if (currentLanguageCode != language.code) {
                        viewModel.onLanguageSelected(language.code)
                    }
                }
            )
        }
    }
}

@Composable
private fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    // CORECTAT: Adăugăm contextul și logica de restart aici
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                onSelect() // Se apelează funcția din ViewModel pentru a seta limba

                // Dacă limba selectată nu este deja cea activă, repornim aplicația
                if (!isSelected) {
                    val intent = Intent(context, MainActivity::class.java)
                    // Flag-urile spun sistemului să creeze o nouă sarcină (task) și să o șteargă pe cea veche
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)

                    // O metodă alternativă și uneori mai "curată" este să închidem doar activitatea curentă
                    // (context as? Activity)?.finish()
                }
            })
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null // Click-ul este gestionat de Row
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = stringResource(id = language.nameResId),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}