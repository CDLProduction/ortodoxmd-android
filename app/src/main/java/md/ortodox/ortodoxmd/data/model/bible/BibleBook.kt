package md.ortodox.ortodoxmd.data.model.bible

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "bible_books")
data class BibleBook(
    @PrimaryKey val id: Long,
    @SerializedName("nameRo") val nameRo: String,
    @SerializedName("nameEn") val nameEn: String,
    @SerializedName("nameRu") val nameRu: String,
    @ColumnInfo(name = "testamentId") val testamentId: Long  // Flatten, remove nested BibleTestament
)