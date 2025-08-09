package md.ortodox.ortodoxmd.ui.saints

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.ui.design.AppEmpty
import md.ortodox.ortodoxmd.ui.design.AppListItem
import md.ortodox.ortodoxmd.ui.design.AppPaddings
import md.ortodox.ortodoxmd.ui.design.AppScaffold

@Composable
fun SaintLivesScreen(
    navController: NavController,
    viewModel: SaintLifeViewModel = hiltViewModel()
) {
    val saintLives by viewModel.saintLives.collectAsState()

    // REFACTORIZAT: Folosim AppScaffold pentru o structură consistentă.
    AppScaffold(title = stringResource(id = R.string.menu_saints_lives)) { paddingValues ->
        if (saintLives.isEmpty()) {
            // REFACTORIZAT: Folosim AppEmpty pentru starea goală.
            AppEmpty(
                message = stringResource(R.string.saints_no_lives_available),
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = AppPaddings.content,
                verticalArrangement = Arrangement.spacedBy(AppPaddings.m)
            ) {
                items(saintLives, key = { it.id }) { life ->
                    // REFACTORIZAT: Folosim AppListItem pentru un aspect standardizat.
                    AppListItem(
                        title = life.nameRo,
                        leading = {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = stringResource(R.string.saints_saint_icon_desc),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailing = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = stringResource(R.string.saints_view_details)
                            )
                        },
                        onClick = { navController.navigate("saint_life_detail/${life.id}") }
                    )
                }
            }
        }
    }
}
