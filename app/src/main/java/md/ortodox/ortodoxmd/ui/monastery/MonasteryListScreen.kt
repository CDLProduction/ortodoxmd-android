package md.ortodox.ortodoxmd.ui.monastery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Church
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.ui.design.AppListItem
import md.ortodox.ortodoxmd.ui.design.AppLoading
import md.ortodox.ortodoxmd.ui.design.AppPaddings
import md.ortodox.ortodoxmd.ui.design.AppScaffold

@Composable
fun MonasteryListScreen(
    navController: NavController,
    viewModel: MonasteryViewModel = hiltViewModel()
) {
    // OPTIMIZARE: Folosim collectAsStateWithLifecycle pentru colectarea sigură și eficientă a stării.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AppScaffold(title = stringResource(id = R.string.menu_monasteries)) { paddingValues ->
        if (uiState.isLoading) {
            AppLoading(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = AppPaddings.content,
                verticalArrangement = Arrangement.spacedBy(AppPaddings.xs)
            ) {
                items(uiState.monasteries, key = { it.id }) { monastery ->
                    AppListItem(
                        title = monastery.nameRo,
                        leading = {
                            Icon(
                                imageVector = Icons.Default.Church,
                                contentDescription = stringResource(R.string.monastery_list_icon_desc),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailing = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = stringResource(R.string.monastery_view_details)
                            )
                        },
                        onClick = {
                            navController.navigate("monastery_detail/${monastery.id}")
                        }
                    )
                    Divider()
                }
            }
        }
    }
}
