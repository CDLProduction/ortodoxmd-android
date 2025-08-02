package md.ortodox.ortodoxmd.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "saint_lives")
data class SaintLife(
    @PrimaryKey
    @SerializedName("id")
    val id: Long,

    @ColumnInfo(name = "name_ro")
    @SerializedName("nameRo")
    val nameRo: String,

    @ColumnInfo(name = "life_description_ro")
    @SerializedName("lifeDescriptionRo")
    val lifeDescriptionRo: String?,

    @ColumnInfo(name = "icon_id")
    @SerializedName("iconId")
    val iconId: Long?
) {
    val formattedLifeDescriptionRo: String
        get() = lifeDescriptionRo?.replace("\\n", "\n") ?: "Biografia nu este disponibilă."
}