package md.ortodox.ortodoxmd.data.model.audiobook

import com.google.gson.annotations.SerializedName

data class AudiobookDto(
    @SerializedName("id") val id: Long,
    @SerializedName("titleRo") val titleRo: String,
    @SerializedName("authorRo") val authorRo: String,
    @SerializedName("filePath") val filePath: String
)