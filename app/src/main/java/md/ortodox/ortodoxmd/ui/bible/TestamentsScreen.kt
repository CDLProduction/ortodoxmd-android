package md.ortodox.ortodoxmd.ui.bible

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.ui.design.AppCard
import md.ortodox.ortodoxmd.ui.design.AppPaddings

@Composable
fun TestamentsScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val testaments = listOf(
        stringResource(R.string.bible_old_testament) to "1",
        stringResource(R.string.bible_new_testament) to "2"
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = AppPaddings.content,
        verticalArrangement = Arrangement.spacedBy(AppPaddings.s)
    ) {
        items(testaments.size) { index ->
            val (name, id) = testaments[index]
            // REFACTORIZAT: Folosim AppCard.
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("bible/books/$id") }
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(AppPaddings.l)
                )
            }
        }
    }
}
