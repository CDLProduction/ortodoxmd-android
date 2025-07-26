package md.ortodox.ortodoxmd.data.model.bible

import androidx.room.ColumnInfo
import androidx.room.Embedded

// NOU: Clasă pentru a uni datele semnului de carte cu detaliile cărții.
data class BookmarkWithDetails(
    @Embedded val bookmark: BibleBookmark,
    @ColumnInfo(name = "nameRo") val bookName: String
)