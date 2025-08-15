package md.ortodox.ortodoxmd.ui.playback

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.KeyEvent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.MainActivity
import md.ortodox.ortodoxmd.R
import md.ortodox.ortodoxmd.data.repository.AudiobookRepository
import javax.inject.Inject

private const val NOTIFICATION_ID = 101
private const val NOTIFICATION_CHANNEL_ID = "ortodoxmd_playback_channel"

@UnstableApi
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var repository: AudiobookRepository

    @Inject
    lateinit var player: ExoPlayer

    private var mediaSession: MediaSession? = null
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (!isPlaying) {
                saveCurrentProgress()
            }
        }

        override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
            saveCurrentProgress()
        }
    }

    override fun onCreate() {
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .setUsage(C.USAGE_MEDIA)
            .build()
        player.setAudioAttributes(audioAttributes, true)
        player.addListener(playerListener)

        // --- START CORECTARE 1: Creăm canalul de notificare ---
        createNotificationChannel()

        val customCallback = object : MediaSession.Callback {
            override fun onMediaButtonEvent(session: MediaSession, controllerInfo: MediaSession.ControllerInfo, mediaButtonIntent: Intent): Boolean {
                val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                }

                if (keyEvent != null && keyEvent.action == KeyEvent.ACTION_DOWN) {
                    when (keyEvent.keyCode) {
                        KeyEvent.KEYCODE_HEADSETHOOK,
                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                            if (player.isPlaying) {
                                player.pause()
                            } else {
                                player.play()
                            }
                            return true
                        }
                    }
                }
                return super.onMediaButtonEvent(session, controllerInfo, mediaButtonIntent)
            }
        }

        val sessionActivityPendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(customCallback)
            .setSessionActivity(sessionActivityPendingIntent)
            .build()

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .setNotificationId(NOTIFICATION_ID)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                // --- START CORECTARE 2: Eliminăm linia care produce eroarea ---
                // .setChannelNameResourceId(R.string.playback_channel_name)
                .build()
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.playback_channel_name), // Numele canalului
                NotificationManager.IMPORTANCE_LOW // Importanță redusă pentru a nu face zgomot
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun saveCurrentProgress() {
        val currentItemIndex = player.currentMediaItemIndex
        if (currentItemIndex == C.INDEX_UNSET) return

        val mediaItem = player.getMediaItemAt(currentItemIndex)
        val chapterId = mediaItem.mediaId.toLongOrNull()
        val position = player.contentPosition

        if (chapterId != null && position > 0) {
            serviceScope.launch {
                repository.savePlaybackPosition(chapterId, position)
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.playWhenReady) {
            saveCurrentProgress()
            stopSelf()
        }
    }

    override fun onDestroy() {
        saveCurrentProgress()
        player.removeListener(playerListener)
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        serviceJob.cancel()
        super.onDestroy()
    }
}