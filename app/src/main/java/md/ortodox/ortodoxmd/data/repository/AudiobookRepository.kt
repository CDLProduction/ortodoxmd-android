package md.ortodox.ortodoxmd.data.repository

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import androidx.lifecycle.asFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import md.ortodox.ortodoxmd.data.AudioDownloadWorker
import md.ortodox.ortodoxmd.data.dao.AudiobookDao
import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity
import md.ortodox.ortodoxmd.data.model.audiobook.LastPlayback
import md.ortodox.ortodoxmd.data.network.AudiobookApiService
import md.ortodox.ortodoxmd.data.network.NetworkModule
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AudiobookRepository @Inject constructor(
    private val apiService: AudiobookApiService,
    private val audiobookDao: AudiobookDao,
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun getDownloadWorkInfoFlow(): Flow<List<WorkInfo>> {
        // FIX: Convertim LiveData la Flow pentru compatibilitate cu WorkManager 2.7.1
        return workManager.getWorkInfosByTagLiveData(DOWNLOAD_TAG).asFlow()
    }

    fun getAudiobooks(): Flow<List<AudiobookEntity>> = audiobookDao.getAll()

    suspend fun getById(id: Long): AudiobookEntity? = audiobookDao.getById(id)

    fun getByIdFlow(id: Long): Flow<AudiobookEntity?> = audiobookDao.getByIdFlow(id)

    fun getLastPlaybackInfo(): Flow<LastPlayback?> = audiobookDao.getLastPlayback()

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
            Log.e("AudiobookRepository", "Failed to sync audiobooks: ${e.message}", e)
        }
    }

    fun startDownload(audiobook: AudiobookEntity) {
        if (audiobook.isDownloaded) {
            Log.d("AudiobookRepository", "Audiobook ${audiobook.id} is already downloaded.")
            return
        }

        val downloadUrl = "${NetworkModule.BASE_URL_AUDIOBOOKS}api/audiobooks/${audiobook.id}/stream"
        val fileName = audiobook.remoteUrlPath.substringAfterLast('/')

        val inputData = workDataOf(
            AudioDownloadWorker.KEY_AUDIOBOOK_ID to audiobook.id,
            AudioDownloadWorker.KEY_DOWNLOAD_URL to downloadUrl,
            AudioDownloadWorker.KEY_FILE_NAME to fileName
        )

        val downloadWorkRequest = OneTimeWorkRequestBuilder<AudioDownloadWorker>()
            .setInputData(inputData)
            .addTag(DOWNLOAD_TAG)
            .addTag("audiobook_download_${audiobook.id}")
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.EXPONENTIAL,
                backoffDelay = 10_000L,
                timeUnit = TimeUnit.MILLISECONDS
            )
            .build()

        Log.d("AudiobookRepository", "Enqueuing download for audiobook ID: ${audiobook.id} with URL: $downloadUrl")
        workManager.enqueueUniqueWork(
            "download_${audiobook.id}",
            ExistingWorkPolicy.KEEP,
            downloadWorkRequest
        )
    }

    suspend fun savePlaybackPosition(id: Long, position: Long) {
        audiobookDao.updatePlaybackPosition(id, position)
        audiobookDao.setLastPlayback(LastPlayback(audiobookId = id, positionMillis = position))
    }

    companion object {
        const val DOWNLOAD_TAG = "audiobook_download"
    }
}