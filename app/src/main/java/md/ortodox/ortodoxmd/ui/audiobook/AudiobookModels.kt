package md.ortodox.ortodoxmd.ui.audiobook

import androidx.work.WorkInfo
import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity
import java.util.Locale

// --- Structuri de date pentru UI ---

data class AudiobooksUiState(
    val categories: List<AudiobookCategory> = emptyList(),
    val isLoading: Boolean = true,
    val downloadStates: Map<Long, WorkInfo.State> = emptyMap(),
    val downloadProgress: Map<Long, Int> = emptyMap()
)

data class ChapterScreenState(
    val book: AudiobookBook? = null,
    val isLoading: Boolean = true
)

data class AudiobookCategory(
    val name: String,
    val books: List<AudiobookBook>
)

data class AudiobookBook(
    val name: String,
    val testament: String,
    val chapters: List<AudiobookEntity>
)

// --- Funcții ajutătoare (Extensii) ---

fun String.toDisplayableName(): String {
    return this.replace('_', ' ').replace('-', ' ').replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}

fun String.fromDisplayableName(): String {
    return this.replace(' ', '_')
}