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
import androidx.work.OneTimeWorkRequest
import androidx.work.await
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import md.ortodox.ortodoxmd.data.worker.AudioDownloadWorker
import md.ortodox.ortodoxmd.data.dao.AudiobookDao
import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity
import md.ortodox.ortodoxmd.data.model.audiobook.LastPlayback
import md.ortodox.ortodoxmd.data.network.AudiobookApiService
import md.ortodox.ortodoxmd.data.network.NetworkModule
import md.ortodox.ortodoxmd.ui.audiobook.toDisplayableName
import java.io.File
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
                    // --- AICI ESTE CORECTAREA ---
                    // Aplicăm formatarea direct pe titlu înainte de a-l salva
                    title = dto.titleRo.toDisplayableName(),
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

    // În interiorul clasei AudiobookRepository

    fun createDownloadWorkRequest(audiobook: AudiobookEntity): OneTimeWorkRequest {
        val downloadUrl = "${NetworkModule.BASE_URL_AUDIOBOOKS}api/audiobooks/${audiobook.id}/stream"
        val fileName = audiobook.remoteUrlPath.substringAfterLast('/')

        return OneTimeWorkRequestBuilder<AudioDownloadWorker>()
            .setInputData(workDataOf(
                AudioDownloadWorker.KEY_AUDIOBOOK_ID to audiobook.id,
                AudioDownloadWorker.KEY_DOWNLOAD_URL to downloadUrl,
                AudioDownloadWorker.KEY_FILE_NAME to fileName
            ))
            .addTag(DOWNLOAD_TAG) // Tag-ul general
            .addTag("audiobook_download_${audiobook.id}") // Tag-ul specific
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.EXPONENTIAL,
                backoffDelay = 10_000L,
                timeUnit = TimeUnit.MILLISECONDS
            )
            .build()
    }

    // Funcția startDownload devine mai simplă
    suspend fun startDownload(audiobook: AudiobookEntity) {
        if (audiobook.isDownloaded) {
            Log.d("AudiobookRepository", "Audiobook ${audiobook.id} is already downloaded.")
            return
        }

        // Curățăm toate sarcinile finalizate (FAILED, CANCELLED, SUCCEEDED)
        // Acest pas este esențial pentru ca reîncercarea să funcționeze corect.
        workManager.pruneWork().await()

        val uniqueWorkName = "download_${audiobook.id}"
        val downloadWorkRequest = createDownloadWorkRequest(audiobook)

        Log.d("AudiobookRepository", "Beginning unique work for audiobook ID: ${audiobook.id}")

        workManager.enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.REPLACE, // REPLACE acum va funcționa cum trebuie
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

    // NOU: Funcție pentru a șterge un singur capitol
    suspend fun deleteChapter(chapter: AudiobookEntity) {
        deleteChapters(listOf(chapter))
    }

    // NOU: Funcție pentru a șterge o listă de capitole
    suspend fun deleteChapters(chapters: List<AudiobookEntity>) {
        val chapterIdsToDelete = mutableListOf<Long>()

        chapters.forEach { chapter ->
            if (chapter.isDownloaded && !chapter.localFilePath.isNullOrEmpty()) {
                try {
                    val fileToDelete = File(chapter.localFilePath!!)
                    if (fileToDelete.exists()) {
                        if (fileToDelete.delete()) {
                            // Adaugă la listă doar dacă fișierul a fost șters cu succes
                            chapterIdsToDelete.add(chapter.id)
                            Log.d("AudiobookRepository", "Deleted file: ${chapter.localFilePath}")
                        } else {
                            Log.e("AudiobookRepository", "Failed to delete file: ${chapter.localFilePath}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AudiobookRepository", "Error deleting file for chapter ${chapter.id}", e)
                }
            }
        }

        // Actualizează baza de date pentru fișierele șterse
        if (chapterIdsToDelete.isNotEmpty()) {
            audiobookDao.markAsNotDownloaded(chapterIdsToDelete)
        }
    }



    fun getDownloadedAudiobooks(): Flow<List<AudiobookEntity>> = audiobookDao.getDownloaded()
}