package md.ortodox.ortodoxmd.data.repository

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import md.ortodox.ortodoxmd.data.dao.AudiobookDao
import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity
import md.ortodox.ortodoxmd.data.model.audiobook.LastPlayback
import md.ortodox.ortodoxmd.data.network.AudiobookApiService
import javax.inject.Inject

class AudiobookRepository @Inject constructor(
    private val apiService: AudiobookApiService,
    private val audiobookDao: AudiobookDao,
    @ApplicationContext private val context: Context
) {
    private val baseUrl = "http://127.0.0.1:8081" // Sau 10.0.2.2 pentru emulator

    fun getAudiobooks(): Flow<List<AudiobookEntity>> = audiobookDao.getAll()

    suspend fun getById(id: Long): AudiobookEntity? = audiobookDao.getById(id)

    suspend fun syncAudiobooks() {
        try {
            val remoteAudiobooks = apiService.getAudiobooks()
            val localAudiobooks = audiobookDao.getAll().first()
            val localMap = localAudiobooks.associateBy { it.id }
            val entitiesToInsert = remoteAudiobooks.map { dto ->
                val existingEntity = localMap[dto.id]
                AudiobookEntity(
                    id = dto.id,
                    title = dto.titleRo,
                    author = dto.authorRo,
                    remoteUrlPath = dto.filePath,
                    localFilePath = existingEntity?.localFilePath,
                    isDownloaded = existingEntity?.isDownloaded ?: false,
                    lastPositionMillis = existingEntity?.lastPositionMillis ?: 0,
                    downloadId = existingEntity?.downloadId ?: -1L
                )
            }
            audiobookDao.insertAll(entitiesToInsert)
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun startDownload(audiobook: AudiobookEntity) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse("$baseUrl/api/audiobooks/${audiobook.id}/stream")
        val fileName = audiobook.remoteUrlPath.substringAfterLast('/')
        val request = DownloadManager.Request(uri)
            .setTitle(audiobook.title)
            .setDescription("Descărcare...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_PODCASTS, fileName)
        val downloadId = downloadManager.enqueue(request)
        audiobookDao.updateDownloadId(audiobook.id, downloadId)
    }

    suspend fun savePlaybackPosition(id: Long, position: Long) {
        audiobookDao.updatePlaybackPosition(id, position)
        // Salvează și ca fiind ULTIMA redare
        audiobookDao.setLastPlayback(LastPlayback(audiobookId = id, positionMillis = position))
    }

    // --- FUNCȚIE NOUĂ PENTRU A OBȚINE ULTIMA REDARE ---
    fun getLastPlaybackInfo(): Flow<LastPlayback?> {
        return audiobookDao.getLastPlayback()
    }
}
