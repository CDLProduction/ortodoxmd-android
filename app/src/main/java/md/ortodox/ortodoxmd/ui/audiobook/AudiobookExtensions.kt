package md.ortodox.ortodoxmd.ui.audiobook

import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity
import java.util.Locale

private fun extractChapterNumber(title: String): Int {
    // Găsește toate numerele din titlu și îl ia pe ultimul.
    // Acest lucru rezolvă corect cazurile ca "1 Cronici - Capitolul 10", alegând "10".
    return "\\d+".toRegex().findAll(title)
        .lastOrNull()
        ?.value
        ?.toIntOrNull() ?: 0
}

fun List<AudiobookEntity>.sortedByChapterNumber(): List<AudiobookEntity> {
    return this.sortedWith(compareBy { extractChapterNumber(it.title) })
}

fun String.toDisplayableName(): String {
    val prefixRegex = "^\\d+\\s*[-_]+\\s*".toRegex()
    return this
        .replaceFirst(prefixRegex, "")
        .replace('_', ' ')
        .replace("–", "-")
        .trim()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}