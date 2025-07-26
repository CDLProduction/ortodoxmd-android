package md.ortodox.ortodoxmd.data.model.bible

import androidx.room.Embedded

data class VerseWithBookInfo(
    @Embedded
    val verse: BibleVerse,
    val bookNameRo: String
)