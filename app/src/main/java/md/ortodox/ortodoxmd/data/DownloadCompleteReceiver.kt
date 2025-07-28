package md.ortodox.ortodoxmd.data

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.dao.AudiobookDao
import javax.inject.Inject

@AndroidEntryPoint
class DownloadCompleteReceiver : BroadcastReceiver() {
    @Inject
    lateinit var audiobookDao: AudiobookDao

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id != -1L) {
                CoroutineScope(Dispatchers.IO).launch {
                    val audiobook = audiobookDao.getByDownloadId(id)
                    audiobook?.let {
                        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        val uri = downloadManager.getUriForDownloadedFile(id)
                        uri?.let {
                            audiobookDao.setAsDownloaded(audiobook.id, it.path ?: "")
                        }
                    }
                }
            }
        }
    }
}
