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
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.compose.material3.MaterialTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudiobookTestamentsScreen(navController: NavController, testaments: List<String>, categoryName: String) {
    Log.d("AudiobookTestamentsScreen", "Rendering with categoryName: $categoryName")
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Testamente: ${categoryName.ifEmpty { "Toate" }}") },
                navigationIcon = {
                    IconButton(onClick = {
                        Log.d("AudiobookTestamentsScreen", "Back button clicked")
                        navController.popBackStack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Înapoi",
                            tint = MaterialTheme.colorScheme.onBackground
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
            items(testaments, key = { it }) { testament ->
                ListItem(
                    headlineContent = { Text(testament) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                    modifier = Modifier.clickable {
                        val encodedTestamentName = URLEncoder.encode(testament, StandardCharsets.UTF_8.toString())
                        navController.navigate("audiobook_books/$encodedTestamentName?testamentName=$encodedTestamentName")
                    }
                )
            }
        }
    }
}