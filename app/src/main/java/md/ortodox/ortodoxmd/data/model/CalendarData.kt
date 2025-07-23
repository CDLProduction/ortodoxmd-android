package md.ortodox.ortodoxmd.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "calendar_data")
data class CalendarData(
    @PrimaryKey val date: String,
    @SerializedName("fastingType") val fastingType: String,
    @SerializedName("fastingDescriptionEn") val fastingDescriptionEn: String,
    @SerializedName("fastingDescriptionRo") val fastingDescriptionRo: String,
    @SerializedName("fastingDescriptionRu") val fastingDescriptionRu: String,
    @SerializedName("summaryTitleEn") val summaryTitleEn: String,
    @SerializedName("summaryTitleRo") val summaryTitleRo: String,
    @SerializedName("summaryTitleRu") val summaryTitleRu: String,
    @SerializedName("titlesEn") val titlesEn: String,
    @SerializedName("titlesRo") val titlesRo: String,
    @SerializedName("titlesRu") val titlesRu: String,
    @SerializedName("saints") val saints: List<Saint>,  // Room nu suportă direct List, va necesita conversie
    @SerializedName("fastingDay") val fastingDay: Boolean
)

data class Saint(
    @SerializedName("id") val id: Int,
    @SerializedName("nameAndDescriptionEn") val nameAndDescriptionEn: String,
    @SerializedName("nameAndDescriptionRo") val nameAndDescriptionRo: String,
    @SerializedName("nameAndDescriptionRu") val nameAndDescriptionRu: String
)