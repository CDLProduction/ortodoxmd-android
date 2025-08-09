package md.ortodox.ortodoxmd.ui.design

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import md.ortodox.ortodoxmd.R

@Composable
fun AppMenuIcon(
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            Icons.Default.Menu,
            contentDescription = stringResource(R.string.menu_open)
        )
    }
}