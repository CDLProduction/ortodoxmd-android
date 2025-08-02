package md.ortodox.ortodoxmd.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "apologetics")
data class Apologetic(
    @PrimaryKey
    @SerializedName("id")
    val id: Long,

    @ColumnInfo(name = "question_ro")
    @SerializedName("questionRo")
    val questionRo: String,

    @ColumnInfo(name = "answer_ro")
    @SerializedName("answerRo")
    val answerRo: String?,

    @SerializedName("category")
    val category: String
) {
    val formattedAnswerRo: String
        get() = answerRo?.replace("\\n", "\n") ?: "Răspunsul nu este disponibil."
}