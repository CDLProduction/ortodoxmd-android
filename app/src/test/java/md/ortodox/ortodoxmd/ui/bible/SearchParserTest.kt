package md.ortodox.ortodoxmd.ui.bible

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchParserTest {
    @Test
    fun parseWithOnlyBookDefaultsToFirstChapter() {
        val result = SearchParser.parse("Geneza")
        assertEquals(
            ParsedReference(
                bookName = "FACEREA (GENEZA)",
                chapter = 1,
                startVerse = 1,
                endVerse = null
            ),
            result
        )
    }
}
