package md.ortodox.ortodoxmd.ui.saints

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

// --- Refactorizat pentru a folosi SaintLife ---
sealed class ContentBlock {
    data class H1(val text: String) : ContentBlock()
    data class H2(val text: String) : ContentBlock()
    data class Paragraph(val text: String) : ContentBlock()
}

@Composable
private fun rememberParsedText(rawText: String?): List<ContentBlock> {
    return remember(rawText) {
        if (rawText == null) return@remember emptyList()
        val blocks = mutableListOf<ContentBlock>()
        val lines = rawText.split("\n").filter { it.isNotBlank() }
        for (line in lines) {
            when {
                line.startsWith("Partea ") -> blocks.add(ContentBlock.H1(line))
                line.length < 80 && line.trim().endsWith(":") -> blocks.add(ContentBlock.H2(line))
                else -> blocks.add(ContentBlock.Paragraph(line))
            }
        }
        blocks
    }
}

enum class ReaderTheme(val background: Color, val onBackground: Color) {
    LIGHT(Color(0xFFFBF8F2), Color(0xFF1B1C18)),
    SEPIA(Color(0xFFF5EEDD), Color(0xFF5A4B3A)),
    DARK(Color(0xFF1B1C18), Color(0xFFE5E2DA))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaintLifeDetailScreen(
    navController: NavController,
    saintLifeId: Long,
    viewModel: SaintLifeDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(key1 = saintLifeId) {
        viewModel.loadSaintLife(saintLifeId)
    }

    val saintLife by viewModel.saintLife.collectAsState()
    var readerTheme by remember { mutableStateOf(ReaderTheme.LIGHT) }
    var fontScale by remember { mutableStateOf(1.0f) }
    val parsedText = rememberParsedText(saintLife?.formattedLifeDescriptionRo)
    val lazyListState = rememberLazyListState()

    val scrollProgress by remember {
        derivedStateOf {
            if (lazyListState.layoutInfo.visibleItemsInfo.isNotEmpty() && lazyListState.layoutInfo.totalItemsCount > 0) {
                val lastVisibleItem = lazyListState.layoutInfo.visibleItemsInfo.last()
                (lastVisibleItem.index + 1).toFloat() / lazyListState.layoutInfo.totalItemsCount.toFloat()
            } else { 0f }
        }
    }

    Scaffold(
        containerColor = readerTheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        saintLife?.let {
                            Text(it.nameRo, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Înapoi", tint = readerTheme.onBackground)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            readerTheme = when (readerTheme) {
                                ReaderTheme.LIGHT -> ReaderTheme.SEPIA
                                ReaderTheme.SEPIA -> ReaderTheme.DARK
                                ReaderTheme.DARK -> ReaderTheme.LIGHT
                            }
                        }) {
                            Icon(Icons.Default.Tonality, "Schimbă Tema", tint = readerTheme.onBackground)
                        }
                        IconButton(onClick = { if (fontScale > 0.8f) fontScale -= 0.1f }) {
                            Icon(Icons.Default.Remove, "Micșorează Text", tint = readerTheme.onBackground)
                        }
                        IconButton(onClick = { if (fontScale < 1.5f) fontScale += 0.1f }) {
                            Icon(Icons.Default.Add, "Mărește Text", tint = readerTheme.onBackground)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
                LinearProgressIndicator(
                    progress = { scrollProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )
            }
        }
    ) { paddingValues ->
        if (saintLife == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize().padding(paddingValues).animateContentSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
            ) {
                items(parsedText) { block ->
                    when (block) {
                        is ContentBlock.H1 -> Text(
                            text = block.text,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = (MaterialTheme.typography.headlineSmall.fontSize.value * fontScale).sp,
                                color = readerTheme.onBackground
                            ),
                            modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
                        )
                        is ContentBlock.H2 -> Text(
                            text = block.text,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = (MaterialTheme.typography.titleLarge.fontSize.value * fontScale).sp,
                                color = readerTheme.onBackground
                            ),
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                        is ContentBlock.Paragraph -> Text(
                            text = block.text,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = (MaterialTheme.typography.bodyLarge.fontSize.value * fontScale).sp,
                                lineHeight = (MaterialTheme.typography.bodyLarge.lineHeight.value * 1.5f * fontScale).sp,
                                color = readerTheme.onBackground
                            ),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }
            }
        }
    }
}