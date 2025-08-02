package md.ortodox.ortodoxmd.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "sacraments")
data class Sacrament(
    @PrimaryKey
    @SerializedName("id")
    val id: Long,

    @ColumnInfo(name = "title_ro")
    @SerializedName("titleRo")
    val titleRo: String,

    @ColumnInfo(name = "description_ro")
    @SerializedName("descriptionRo")
    val descriptionRo: String?,

    @SerializedName("category")
    val category: String
) {
    val formattedDescriptionRo: String
        get() = descriptionRo?.replace("\\n", "\n") ?: "Descriere lipsă."
}