package md.ortodox.ortodoxmd.data.model.bible

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bible_bookmarks")
data class BibleBookmark(
    @PrimaryKey val id: Int = 1,  // Single bookmark
    val bookId: Long,
    val chapterNumber: Int,
    val verseId: Long
)