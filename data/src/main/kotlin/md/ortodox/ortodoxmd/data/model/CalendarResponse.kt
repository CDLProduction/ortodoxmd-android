package md.ortodox.ortodoxmd.data.model

import com.google.gson.annotations.SerializedName

data class CalendarResponse(
    val date: String,
    val fastingType: String,
    val fastingDescriptionEn: String,
    val fastingDescriptionRo: String,
    val fastingDescriptionRu: String,
    val summaryTitleEn: String,
    val summaryTitleRo: String,
    val summaryTitleRu: String,
    val titlesEn: String,
    val titlesRo: String,
    val titlesRu: String,
    val saints: List<Saint>,
    val fastingDay: Boolean
)

data class Saint(
    val id: Int,
    val nameAndDescriptionEn: String,
    val nameAndDescriptionRo: String,
    val nameAndDescriptionRu: String
)