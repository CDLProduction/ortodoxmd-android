package md.ortodox.ortodoxmd.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "saints")
data class Saint(
    @PrimaryKey val id: Long,

    @ColumnInfo(name = "name_and_description_en")
    @SerializedName("name_and_description_en", alternate = ["nameAndDescriptionEn"])
    val nameAndDescriptionEn: String,

    @ColumnInfo(name = "name_and_description_ro")
    @SerializedName("name_and_description_ro", alternate = ["nameAndDescriptionRo"])
    val nameAndDescriptionRo: String,

    @ColumnInfo(name = "name_and_description_ru")
    @SerializedName("name_and_description_ru", alternate = ["nameAndDescriptionRu"])
    val nameAndDescriptionRu: String,

    @ColumnInfo(name = "icon_id")
    @SerializedName("icon_id", alternate = ["iconId"])
    val iconId: Long? = null
)