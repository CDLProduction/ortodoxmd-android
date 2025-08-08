package md.ortodox.ortodoxmd.ui.settings

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.util.LocaleHelper

@Composable
fun LanguageSettingsScreen(viewModel: LanguageViewModel = hiltViewModel()) {
    val current by viewModel.language.collectAsState()
    val context = LocalContext.current
    val activity = context as Activity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = stringResource(R.string.select_language), style = MaterialTheme.typography.titleLarge)
        LanguageOption("ro", stringResource(R.string.language_romanian), current == "ro") {
            viewModel.setLanguage("ro")
            LocaleHelper.applyLanguage(context, "ro")
            activity.recreate()
        }
        LanguageOption("ru", stringResource(R.string.language_russian), current == "ru") {
            viewModel.setLanguage("ru")
            LocaleHelper.applyLanguage(context, "ru")
            activity.recreate()
        }
        LanguageOption("en", stringResource(R.string.language_english), current == "en") {
            viewModel.setLanguage("en")
            LocaleHelper.applyLanguage(context, "en")
            activity.recreate()
        }
    }
}

@Composable
private fun LanguageOption(code: String, label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(
            text = label,
            modifier = Modifier
                .padding(start = 8.dp)
                .clickable { onSelect() }
        )
    }
}
