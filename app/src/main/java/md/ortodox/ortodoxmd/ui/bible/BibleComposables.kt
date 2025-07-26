package md.ortodox.ortodoxmd.ui.bible

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Composable partajat pentru afișarea unui singur verset.
 * Poate fi folosit în ecranul de Căutare, Semne de Carte și în vizualizarea unui capitol.
 *
 * @param verseNumber Numărul versetului.
 * @param verseText Textul versetului.
 * @param reference Referința (ex: "Geneza 1"), opțională.
 * @param modifier Modifier pentru personalizare.
 */
@Composable
fun VerseItem(
    verseNumber: String,
    verseText: String,
    reference: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = verseNumber,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(24.dp)
        )
        Column {
            if (reference != null) {
                Text(
                    text = reference,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(
                text = verseText,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.fontSize * 1.5
            )
        }
    }
}
