package md.ortodox.ortodoxmd.ui.language

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.ui.design.AppListItem
import md.ortodox.ortodoxmd.ui.design.AppPaddings
import md.ortodox.ortodoxmd.ui.design.AppScaffold

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
    val context = LocalContext.current
    val activity = context as? Activity

    AppScaffold(
        title = stringResource(id = R.string.menu_language)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = AppPaddings.content,
            // ADAUGAT: Spațiere între carduri pentru un aspect mai aerisit.
            verticalArrangement = Arrangement.spacedBy(AppPaddings.m)
        ) {
            items(supportedLanguages) { language ->
                val isSelected = language.code == currentLanguageCode

                AppListItem(
                    title = stringResource(id = language.nameResId),
                    leading = {
                        RadioButton(
                            selected = isSelected,
                            onClick = null // Click-ul este gestionat de întregul element.
                        )
                    },
                    onClick = {
                        if (!isSelected) {
                            viewModel.onLanguageSelected(language.code)
                            activity?.recreate() // Logica de recreare a activității rămâne.
                        }
                    }
                )
                // ELIMINAT: Divider-ul nu mai este necesar.
            }
        }
    }
}
