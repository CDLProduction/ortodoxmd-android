package md.ortodox.ortodoxmd.data.model

import com.google.gson.annotations.SerializedName

data class CalendarData(
    @SerializedName("date") val date: String,
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
    @SerializedName("saints") val saints: List<Saint>,
    @SerializedName("fastingDay") val fastingDay: Boolean
)

data class Saint(
    @SerializedName("id") val id: Int,
    @SerializedName("nameAndDescriptionEn") val nameAndDescriptionEn: String,
    @SerializedName("nameAndDescriptionRo") val nameAndDescriptionRo: String,
    @SerializedName("nameAndDescriptionRu") val nameAndDescriptionRu: String
)