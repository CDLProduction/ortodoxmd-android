package md.ortodox.ortodoxmd.ui.audiobook

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.MaterialTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudiobookCategoriesScreen(navController: NavController, categories: List<AudiobookCategory>, categoryName: String) {
    Log.d("AudiobookCategoriesScreen", "Rendering with categoryName: $categoryName")
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorii: $categoryName") },
                navigationIcon = {
                    IconButton(onClick = {
                        Log.d("AudiobookCategoriesScreen", "Back button clicked")
                        navController.popBackStack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Înapoi",
                            tint = MaterialTheme.colorScheme.onBackground // Culoare dinamică bazată pe temă
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(paddingValues)
        ) {
            items(categories, key = { it.name }) { category ->
                ListItem(
                    headlineContent = { Text(category.name) },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = null) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                    modifier = Modifier.clickable {
                        val encodedCategoryName = URLEncoder.encode(category.name, StandardCharsets.UTF_8.toString())
                        navController.navigate("audiobook_testaments/${category.name}?categoryName=$encodedCategoryName")
                    }
                )
            }
        }
    }
}