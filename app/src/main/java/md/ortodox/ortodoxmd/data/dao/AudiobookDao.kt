package md.ortodox.ortodoxmd.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity
import md.ortodox.ortodoxmd.data.model.audiobook.LastPlayback

@Dao
interface AudiobookDao {
    @Upsert
    suspend fun insertAll(audiobooks: List<AudiobookEntity>)

    @Query("SELECT * FROM audiobooks")
    fun getAll(): Flow<List<AudiobookEntity>>

    @Query("SELECT * FROM audiobooks WHERE id = :id")
    suspend fun getById(id: Long): AudiobookEntity?

    @Query("SELECT * FROM audiobooks WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<AudiobookEntity?>

    @Query("UPDATE audiobooks SET lastPositionMillis = :position WHERE id = :id")
    suspend fun updatePlaybackPosition(id: Long, position: Long)

    @Query("UPDATE audiobooks SET localFilePath = :path, isDownloaded = 1, downloadId = -1 WHERE id = :id")
    suspend fun setAsDownloaded(id: Long, path: String)

    @Query("UPDATE audiobooks SET downloadId = :downloadId WHERE id = :id")
    suspend fun updateDownloadId(id: Long, downloadId: Long)

    @Query("SELECT * FROM audiobooks WHERE downloadId = :downloadId")
    suspend fun getByDownloadId(downloadId: Long): AudiobookEntity?

    @Upsert
    suspend fun setLastPlayback(lastPlayback: LastPlayback)

    @Query("SELECT * FROM last_playback WHERE id = 1")
    fun getLastPlayback(): Flow<LastPlayback?>
}