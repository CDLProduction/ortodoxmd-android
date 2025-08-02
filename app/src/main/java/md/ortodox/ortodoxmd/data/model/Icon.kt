package md.ortodox.ortodoxmd.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "icons")
data class Icon(
    @PrimaryKey val id: Long,

    @SerializedName("name_ro", alternate = ["nameRo"])
    val nameRo: String,

    @SerializedName("name_en", alternate = ["nameEn"])
    val nameEn: String,

    @SerializedName("name_ru", alternate = ["nameRu"])
    val nameRu: String,

    @SerializedName("file_path", alternate = ["filePath"])
    val filePath: String,

    @SerializedName("category")
    val category: String
)