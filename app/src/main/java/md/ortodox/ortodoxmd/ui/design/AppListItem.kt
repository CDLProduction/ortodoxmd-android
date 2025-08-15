package md.ortodox.ortodoxmd.ui.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListItem(
    title: String,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit, // onClick este acum obligatoriu pentru card
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = AppPaddings.xs),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // ADAUGAT: Un Box pentru a adăuga spațiere verticală și a mări cardul.
        Box(modifier = Modifier.padding(vertical = 4.dp)) {
            ListItem(
                headlineContent = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                supportingContent = subtitle?.let { { Text(it, maxLines = 2, overflow = TextOverflow.Ellipsis) } },
                leadingContent = leading,
                trailingContent = trailing,
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent // Fundal transparent pentru a vedea culoarea cardului
                )
            )
        }
    }
}
