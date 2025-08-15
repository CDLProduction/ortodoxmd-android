package md.ortodox.ortodoxmd.ui.design

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    // ADAUGAT: Parametru pentru a putea adăuga butoane de acțiune flotante
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = { AppTopBar(title = title, onBack = onBack, actions = actions) },
        floatingActionButton = floatingActionButton // ADAUGAT: Pasăm FAB-ul către Scaffold
    ) { innerPadding ->
        content(innerPadding)
    }
}
