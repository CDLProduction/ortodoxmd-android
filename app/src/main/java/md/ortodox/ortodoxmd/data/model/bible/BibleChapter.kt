package md.ortodox.ortodoxmd.data.model.bible

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "bible_chapters")
data class BibleChapter(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,  // Auto-generate ID if not from API
    @ColumnInfo(name = "bookId") val bookId: Long,  // Adăugat pentru foreign key/query
    @SerializedName("chapterNumber") val chapterNumber: Int
)