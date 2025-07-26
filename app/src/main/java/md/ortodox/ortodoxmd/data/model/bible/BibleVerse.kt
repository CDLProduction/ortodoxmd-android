package md.ortodox.ortodoxmd.data.model.bible

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "bible_verses")
data class BibleVerse(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "bookId") val bookId: Long,  // Adăugat pentru foreign key/query
    @ColumnInfo(name = "chapterNumber") val chapterNumber: Int,  // Adăugat pentru foreign key/query
    @SerializedName("verseNumber") val verseNumber: Int,
    @SerializedName("textRo") val textRo: String,
    @SerializedName("textEn") val textEn: String?,
    @SerializedName("textRu") val textRu: String?
) {
    val formattedTextRo: String
        get() = textRo.replace("\\n", "\n")
}