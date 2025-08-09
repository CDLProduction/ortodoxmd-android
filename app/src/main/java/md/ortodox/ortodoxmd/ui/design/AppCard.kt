package md.ortodox.ortodoxmd.ui.design

import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    // ADAUGAT: Parametru opțional pentru a face cardul clicabil
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }

    Card(
        shape = CardDefaults.shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Am crescut puțin elevația pentru a se potrivi cu designul original
        modifier = cardModifier
    ) { content() }
}
