package md.ortodox.ortodoxmd.ui.bible

import java.util.regex.Pattern

data class ParsedReference(
    val bookName: String,
    val chapter: Int,
    val startVerse: Int,
    val endVerse: Int? = null
)

object SearchParser {
    // Expresie regulată pentru a extrage numerele (capitol, verset-verset)
    // Ex: "2:15", "2:15-16", "2"
    private val REF_PATTERN = Pattern.compile("^(\\d+)(?:\\s*:\\s*(\\d+)(?:\\s*-\\s*(\\d+))?)?")

    fun parse(query: String): ParsedReference? {
        // Pasul 1: Încercăm să găsim numele cărții folosind noul nostru mapper
        val bookMatch = BibleBookMapper.findBook(query) ?: return null

        val canonicalBookName = bookMatch.first
        val numbersPart = bookMatch.second

        // Dacă utilizatorul a introdus doar numele cărții fără referințe numerice,
        // presupunem capitolul 1, versetul 1.
        if (numbersPart.isBlank()) {
            return ParsedReference(
                bookName = canonicalBookName,
                chapter = 1,
                startVerse = 1,
                endVerse = null
            )
        }

        // Pasul 2: Aplicăm expresia regulată pe restul textului (partea cu numere)
        val matcher = REF_PATTERN.matcher(numbersPart)
        if (!matcher.find()) return null

        return try {
            val chapter = matcher.group(1)?.toInt() ?: return null
            val startVerse = matcher.group(2)?.toInt() ?: 1 // Dacă nu e specificat versetul, începem cu 1
            val endVerse = matcher.group(3)?.toInt()

            ParsedReference(
                bookName = canonicalBookName,
                chapter = chapter,
                startVerse = startVerse,
                endVerse = endVerse
            )
        } catch (e: NumberFormatException) {
            null
        }
    }
}