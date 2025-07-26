package md.ortodox.ortodoxmd.ui.bible

data class ParsedReference(
    val bookName: String,
    val chapter: Int,
    val startVerse: Int,
    val endVerse: Int? = null
)

object SearchParser {
    // Expresie regulată pentru "NumeCarte Capitol:Verset" sau "NumeCarte Capitol:Verset-Verset"
    private val referenceRegex = """^(.+?)\s+(\d+):(\d+)(?:-(\d+))?$""".toRegex()

    fun parse(query: String): ParsedReference? {
        val match = referenceRegex.find(query.trim()) ?: return null

        return try {
            // Extrage grupurile: 1=Nume Carte, 2=Capitol, 3=Verset Start, 4=Verset Sfârșit (opțional)
            val bookName = match.groupValues[1].trim()
            val chapter = match.groupValues[2].toInt()
            val startVerse = match.groupValues[3].toInt()
            val endVerse = match.groupValues[4].takeIf { it.isNotEmpty() }?.toInt()

            ParsedReference(bookName, chapter, startVerse, endVerse)
        } catch (e: Exception) {
            null
        }
    }
}