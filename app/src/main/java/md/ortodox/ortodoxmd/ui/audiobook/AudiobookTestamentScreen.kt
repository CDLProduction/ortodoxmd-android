package md.ortodox.ortodoxmd.ui.audiobook

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.ui.design.AppCard
import md.ortodox.ortodoxmd.ui.design.AppPaddings
import md.ortodox.ortodoxmd.ui.design.AppScaffold
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AudiobookTestamentsScreen(navController: NavController, testaments: List<String>, categoryName: String) {
    // REFACTORIZAT: Folosim AppScaffold.
    AppScaffold(
        title = stringResource(R.string.audiobook_testaments_title, categoryName.ifEmpty { stringResource(R.string.common_all) }),
        onBack = { navController.popBackStack() }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = AppPaddings.content,
            verticalArrangement = Arrangement.spacedBy(AppPaddings.m),
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            items(testaments, key = { it }) { testament ->
                // REFACTORIZAT: Folosim AppCard.
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val encodedTestamentName = URLEncoder.encode(testament, StandardCharsets.UTF_8.toString())
                        navController.navigate("audiobook_books/$encodedTestamentName?testamentName=$encodedTestamentName")
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(AppPaddings.l),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.LibraryBooks,
                            contentDescription = stringResource(R.string.audiobook_testament_icon_desc),
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = testament,
                            modifier = Modifier.weight(1f).padding(start = AppPaddings.l),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.common_navigate),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}
