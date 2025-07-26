package md.ortodox.ortodoxmd.data.model.bible

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "bible_testaments")
data class BibleTestament(
    @PrimaryKey val id: Long,
    @SerializedName("nameRo") val nameRo: String,
    @SerializedName("nameEn") val nameEn: String,
    @SerializedName("nameRu") val nameRu: String
)