package md.ortodox.ortodoxmd.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Entity(tableName = "prayers")
data class Prayer(
    @PrimaryKey val id: Int,
    val parentId: Int? = null,
    @SerializedName("titleEn") val titleEn: String,
    @SerializedName("titleRo") val titleRo: String,
    @SerializedName("titleRu") val titleRu: String,
    @SerializedName("textEn") val textEn: String,
    @SerializedName("textRo") val rawTextRo: String,
    @SerializedName("textRu") val textRu: String,
    @SerializedName("category") val category: String,
    @SerializedName("orderIndex") val orderIndex: Int,
    @SerializedName("subPrayers") val subPrayers: List<Prayer>
) {
    val textRo: String
        get() = rawTextRo.replace("\\n", "\n")
}