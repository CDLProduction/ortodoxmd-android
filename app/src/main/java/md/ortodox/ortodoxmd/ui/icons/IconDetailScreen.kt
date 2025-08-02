package md.ortodox.ortodoxmd.ui.icons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import md.ortodox.ortodoxmd.data.network.NetworkModule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconDetailScreen(
    navController: NavController,
    iconId: Long,
    viewModel: IconDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(key1 = iconId) {
        viewModel.loadIcon(iconId)
    }

    val icon by viewModel.icon.collectAsState()
    val imageUrl = icon?.let { "${NetworkModule.BASE_URL_AUDIOBOOKS}api/icons/${it.id}/stream" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Înapoi",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Imagine de fundal estompată
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(radius = 24.dp)
                )
                // Gradient pentru a întuneca marginile
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                startY = 0.0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
            }

            // Conținutul principal (imaginea clară și textul)
            AnimatedVisibility(visible = icon != null, enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(1000))) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.weight(0.5f))

                    // Imaginea principală, clară
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .aspectRatio(0.75f),
                        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = icon?.nameRo,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Numele icoanei într-o casetă
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        color = Color.Black.copy(alpha = 0.5f),
                    ) {
                        Text(
                            text = icon?.nameRo ?: "",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                        )
                    }
                }
            }

            if (icon == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}