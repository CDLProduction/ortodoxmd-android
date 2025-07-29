package md.ortodox.ortodoxmd.ui.audiobook

import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity

data class AudiobookCategory(
    val name: String,
    val books: List<AudiobookBook>
)

data class AudiobookBook(
    val name: String,
    val testament: String,
    val chapters: List<AudiobookEntity>
)

fun String.toDisplayableName(): String {
    return this.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
