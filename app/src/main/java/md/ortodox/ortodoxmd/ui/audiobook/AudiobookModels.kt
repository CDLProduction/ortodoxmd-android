package md.ortodox.ortodoxmd.ui.audiobook

import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity

// Reprezintă o categorie principală, de ex. "Biblia"
data class AudiobookCategory(
    val name: String,
    val books: List<AudiobookBook>
)

// Reprezintă o carte anume, de ex. "Evanghelia după Ioan"
data class AudiobookBook(
    val name: String, // Numele afișat, ex. "Evanghelia după Ioan"
    val testament: String, // ex. "Noul Testament"
    val chapters: List<AudiobookEntity> // Capitolele (fișierele audio) din carte
)

// Funcție ajutătoare pentru a converti numele tehnic în nume afișabil
fun String.toDisplayableName(): String {
    return this.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
