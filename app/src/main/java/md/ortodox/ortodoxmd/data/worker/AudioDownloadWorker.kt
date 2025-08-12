package md.ortodox.ortodoxmd.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.data.dao.AudiobookDao
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

@HiltWorker
class AudioDownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val audiobookDao: AudiobookDao,
    private val okHttpClient: OkHttpClient
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_AUDIOBOOK_ID = "audiobook_id"
        const val KEY_DOWNLOAD_URL = "download_url"
        const val KEY_FILE_NAME = "file_name"
        const val KEY_PROGRESS = "progress"
        const val NOTIFICATION_CHANNEL_ID = "audio_download_channel"
        const val PROGRESS_MAX = 100
    }

    override suspend fun doWork(): Result {
        val audiobookId = inputData.getLong(KEY_AUDIOBOOK_ID, -1L)
        val downloadUrl = inputData.getString(KEY_DOWNLOAD_URL) ?: ""
        val fileName = inputData.getString(KEY_FILE_NAME) ?: ""

        if (audiobookId == -1L || downloadUrl.isEmpty() || fileName.isEmpty()) {
            Log.e("AudioDownloadWorker", "Invalid input data")
            return Result.failure()
        }

        val notificationId = audiobookId.toInt()

        return try {
            val initialForegroundInfo = createForegroundInfo(notificationId, 0, "Descărcare în așteptare...")
            setForeground(initialForegroundInfo)
            Log.d("AudioDownloadWorker", "Starting download for audiobook ID: $audiobookId")

            val request = Request.Builder().url(downloadUrl).build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.e("AudioDownloadWorker", "Download failed: Server code ${response.code}")
                return Result.retry()
            }

            val destinationDir = context.getExternalFilesDir(null) ?: context.filesDir
            val destinationFile = File(destinationDir, fileName)

            val totalBytes = response.body?.contentLength() ?: -1L
            var downloadedBytes = 0L

            // --- START: OPTIMIZARE ---
            var lastReportedProgress = -1
            // --- END: OPTIMIZARE ---

            withContext(Dispatchers.IO) {
                response.body?.byteStream()?.use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        val buffer = ByteArray(8192) // 8KB buffer
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            val progress = if (totalBytes > 0) (downloadedBytes * PROGRESS_MAX / totalBytes).toInt() else 0

                            // --- START: OPTIMIZARE ---
                            // Trimitem actualizarea doar dacă procentul s-a schimbat.
                            if (progress > lastReportedProgress) {
                                lastReportedProgress = progress
                                val progressData = Data.Builder().putInt(KEY_PROGRESS, progress).build()
                                setProgress(progressData)

                                val foregroundInfo = createForegroundInfo(notificationId, progress, "Descărcare... $progress%")
                                setForeground(foregroundInfo)
                            }
                            // --- END: OPTIMIZARE ---
                        }
                    }
                }
            }

            val filePath = destinationFile.absolutePath
            audiobookDao.setAsDownloaded(audiobookId, filePath)
            Log.d("AudioDownloadWorker", "SUCCESS for ID $audiobookId, saved at: $filePath")

            Result.success()

        } catch (e: Exception) {
            Log.e("AudioDownloadWorker", "Exception for ID $audiobookId", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun createForegroundInfo(notificationId: Int, progress: Int, progressText: String): ForegroundInfo {
        val channelId = NOTIFICATION_CHANNEL_ID
        val channelName = "Descărcări Audio"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Descărcare: ${inputData.getString(KEY_FILE_NAME)}")
            .setContentText(progressText)
            .setSmallIcon(R.drawable.ic_notification_radio)
            .setProgress(PROGRESS_MAX, progress, false)
            .setOngoing(true)
            .setSilent(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }
}