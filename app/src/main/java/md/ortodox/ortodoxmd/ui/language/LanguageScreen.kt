package md.ortodox.ortodoxmd.ui.language

import android.app.Activity
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
    val context = LocalContext.current
    val activity = context as? Activity
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                if (!isSelected) {
                    onSelect()
                    activity?.recreate()
                }
            })
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = stringResource(id = language.nameResId),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}