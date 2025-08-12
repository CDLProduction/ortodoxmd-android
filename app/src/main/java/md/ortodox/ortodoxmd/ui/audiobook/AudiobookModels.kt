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
    val books: List<AudiobookBook>,
    val isSimpleCategory: Boolean = false
)

data class AudiobookBook(
    val name: String,
    val testament: String,
    val chapters: List<AudiobookEntity>
)

// --- START: LOGICA CORECTATĂ ---
fun String.toDisplayableName(): String {
    // Regex pentru a găsi și elimina prefixele numerice (ex: "01_", "10 - ", "04_")
    val prefixRegex = "^\\d+\\s*[-_]+\\s*".toRegex()

    return this
        .replaceFirst(prefixRegex, "") // Elimină prefixul
        .replace('_', ' ')           // Înlocuiește underscore cu spațiu
        .replace("–", "-")          // Uniformizează cratimele (opțional, dar recomandat)
        .trim()                        // Elimină spațiile de la început sau sfârșit
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } // Capitalizează prima literă
}
// --- END: LOGICA CORECTATĂ ---


fun String.fromDisplayableName(): String {
    return this.replace(' ', '_')
}