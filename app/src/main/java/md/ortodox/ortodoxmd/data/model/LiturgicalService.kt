package md.ortodox.ortodoxmd.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "liturgical_services")
data class LiturgicalService(
    @PrimaryKey
    @SerializedName("id")
    val id: Long,

    @ColumnInfo(name = "calendar_date")
    @SerializedName("calendarDate")
    val calendarDate: String,

    @ColumnInfo(name = "service_type")
    @SerializedName("serviceType")
    val serviceType: String,

    @ColumnInfo(name = "details_ru")
    @SerializedName("detailsRu")
    val detailsRu: String?,

    @ColumnInfo(name = "details_ro")
    @SerializedName("detailsRo")
    val detailsRo: String?,

    @ColumnInfo(name = "details_en")
    @SerializedName("detailsEn")
    val detailsEn: String?
) {
    // Proactiv, adăugăm proprietăți pentru a formata textul cu paragrafe
    val formattedDetailsRu: String
        get() = detailsRu?.replace("\\n", "\n") ?: ""

    val formattedDetailsRo: String
        get() = detailsRo?.replace("\\n", "\n") ?: ""

    val formattedDetailsEn: String
        get() = detailsEn?.replace("\\n", "\n") ?: ""
}