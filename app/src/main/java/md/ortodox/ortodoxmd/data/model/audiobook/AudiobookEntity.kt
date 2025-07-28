package md.ortodox.ortodoxmd.data.model.audiobook

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audiobooks")
data class AudiobookEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val author: String,
    val remoteUrlPath: String,
    var localFilePath: String? = null,
    var lastPositionMillis: Long = 0,
    var isDownloaded: Boolean = false,
    var downloadId: Long = -1L
)
