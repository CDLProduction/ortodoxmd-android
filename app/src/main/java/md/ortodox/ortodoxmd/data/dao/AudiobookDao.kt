package md.ortodox.ortodoxmd.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity

@Dao
interface AudiobookDao {
    @Upsert
    suspend fun insertAll(audiobooks: List<AudiobookEntity>)
    @Query("SELECT * FROM audiobooks")
    fun getAll(): Flow<List<AudiobookEntity>>
    @Query("SELECT * FROM audiobooks WHERE id = :id")
    suspend fun getById(id: Long): AudiobookEntity?
    @Query("UPDATE audiobooks SET lastPositionMillis = :position WHERE id = :id")
    suspend fun updatePlaybackPosition(id: Long, position: Long)
    @Query("UPDATE audiobooks SET localFilePath = :path, isDownloaded = 1, downloadId = -1 WHERE id = :id")
    suspend fun setAsDownloaded(id: Long, path: String)
    @Query("UPDATE audiobooks SET downloadId = :downloadId WHERE id = :id")
    suspend fun updateDownloadId(id: Long, downloadId: Long)
    @Query("SELECT * FROM audiobooks WHERE downloadId = :downloadId")
    suspend fun getByDownloadId(downloadId: Long): AudiobookEntity?
}
