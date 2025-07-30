package md.ortodox.ortodoxmd.data.model.audiobook

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "last_playback")
data class LastPlayback(
    @PrimaryKey val id: Int = 1,
    val audiobookId: Long,
    val positionMillis: Long
)