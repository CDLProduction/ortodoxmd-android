package md.ortodox.ortodoxmd.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "monasteries")
data class Monastery(
    @PrimaryKey
    @SerializedName("id")
    val id: Long,

    @ColumnInfo(name = "name_ro")
    @SerializedName("nameRo")
    val nameRo: String,

    @ColumnInfo(name = "name_en")
    @SerializedName("nameEn")
    val nameEn: String?,

    @ColumnInfo(name = "name_ru")
    @SerializedName("nameRu")
    val nameRu: String?,

    @ColumnInfo(name = "description_ro")
    @SerializedName("descriptionRo")
    val descriptionRo: String?,

    @ColumnInfo(name = "description_en")
    @SerializedName("descriptionEn")
    val descriptionEn: String?,

    @ColumnInfo(name = "description_ru")
    @SerializedName("descriptionRu")
    val descriptionRu: String?,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double
) {
    val formattedDescriptionRo: String
        get() = descriptionRo?.replace("\\n", "\n") ?: "Descriere lipsă."
}