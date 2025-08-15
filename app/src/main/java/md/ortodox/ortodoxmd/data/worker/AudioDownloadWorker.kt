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
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.data.dao.AudiobookDao
import md.ortodox.ortodoxmd.data.di.DownloadOkHttpClient
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@HiltWorker
class AudioDownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val audiobookDao: AudiobookDao,
    @DownloadOkHttpClient private val okHttpClient: OkHttpClient
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_AUDIOBOOK_ID = "audiobook_id"
        const val KEY_DOWNLOAD_URL = "download_url"
        const val KEY_FILE_NAME = "file_name"
        const val KEY_PROGRESS = "progress"
        private const val NOTIFICATION_CHANNEL_ID = "audio_download_channel"
        private const val PROGRESS_MAX = 100
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val audiobookId = inputData.getLong(KEY_AUDIOBOOK_ID, -1L)
        val fileName = inputData.getString(KEY_FILE_NAME) ?: "Descărcare..."
        return createForegroundInfo(audiobookId.toInt(), 0, fileName, "Descărcare în așteptare...")
    }

    override suspend fun doWork(): Result {
        val audiobookId = inputData.getLong(KEY_AUDIOBOOK_ID, -1L)
        val downloadUrl = inputData.getString(KEY_DOWNLOAD_URL)
        val fileName = inputData.getString(KEY_FILE_NAME)

        if (audiobookId == -1L || downloadUrl.isNullOrEmpty() || fileName.isNullOrEmpty()) {
            return Result.failure()
        }

        Log.d("AudioDownloadWorker", "Starting download for audiobook ID: $audiobookId")
        val destinationFile = File(context.filesDir, fileName)

        try {
            val request = Request.Builder().url(downloadUrl).build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.e("AudioDownloadWorker", "Download failed: Server code ${response.code}")
                return Result.retry()
            }

            val body = response.body ?: return Result.failure()
            val totalBytes = body.contentLength()
            var downloadedBytes = 0L
            var lastReportedProgress = -1
            var lastUpdateTime = 0L // Variabilă pentru a limita frecvența actualizărilor

            withContext(Dispatchers.IO) {
                body.byteStream().use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            if (isStopped) { throw IOException("Work cancelled") }
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            val progress = if (totalBytes > 0) (downloadedBytes * 100 / totalBytes).toInt() else -1

                            // --- AICI ESTE OPTIMIZAREA ---
                            val currentTime = System.currentTimeMillis()
                            // Actualizăm starea doar dacă progresul s-a schimbat ȘI au trecut cel puțin 500ms
                            if (progress != -1 && progress > lastReportedProgress && (currentTime - lastUpdateTime > 500)) {
                                lastUpdateTime = currentTime
                                lastReportedProgress = progress
                                setProgress(workDataOf(KEY_PROGRESS to progress))
                                val foregroundInfo = createForegroundInfo(audiobookId.toInt(), progress, fileName, "Descărcare... $progress%")
                                setForeground(foregroundInfo)
                            }
                        }
                    }
                }
            }

            // Asigurăm o ultimă actualizare la 100%
            setProgress(workDataOf(KEY_PROGRESS to 100))
            audiobookDao.setAsDownloaded(audiobookId, destinationFile.absolutePath)
            Log.d("AudioDownloadWorker", "SUCCESS for ID $audiobookId, saved at: ${destinationFile.absolutePath}")
            val finalNotification = createNotification(audiobookId.toInt(), 100, fileName, "Descărcare finalizată").build()
            notificationManager.notify(audiobookId.toInt(), finalNotification)
            return Result.success()

        } catch (e: Exception) {
            Log.e("AudioDownloadWorker", "Exception for ID $audiobookId", e)
            if (destinationFile.exists()) {
                destinationFile.delete()
            }
            return if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun createForegroundInfo(notificationId: Int, progress: Int, title: String, contentText: String): ForegroundInfo {
        val notification = createNotification(notificationId, progress, title, contentText).build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    private fun createNotification(notificationId: Int, progress: Int, title: String, contentText: String): NotificationCompat.Builder {
        val channelId = NOTIFICATION_CHANNEL_ID
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Descărcări Audio", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification_radio)
            .setOngoing(progress < 100)
            .setAutoCancel(progress == 100)
            .setOnlyAlertOnce(true)
            .setProgress(PROGRESS_MAX, progress, progress == -1)
    }
}