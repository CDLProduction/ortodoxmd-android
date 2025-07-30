package md.ortodox.ortodoxmd.ui.playback

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.view.KeyEvent
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.google.common.util.concurrent.Futures
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
    private lateinit var notificationManager: PlayerNotificationManager
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (!isPlaying) {
                saveCurrentProgress()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        player.addListener(playerListener)

        // --- START CORECȚIE ---
        // Creăm un callback personalizat pentru a intercepta evenimentele de la butoanele media
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
                        // Verificăm ambele coduri de taste posibile pentru Play/Pauză de la căști/AUX
                        KeyEvent.KEYCODE_HEADSETHOOK,
                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                            // Consumăm evenimentul, dar nu facem nimic.
                            // Astfel, semnalul "fantomă" de la mașină este ignorat.
                            return true
                        }
                    }
                }
                // Pentru orice alt buton (ex: de pe o telecomandă bluetooth), lăsăm comportamentul standard
                return super.onMediaButtonEvent(session, controllerInfo, mediaButtonIntent)
            }
        }

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(customCallback) // Setăm callback-ul nostru personalizat
            .build()
        // --- FINAL CORECȚIE ---

        notificationManager = PlayerNotificationManager.Builder(this, NOTIFICATION_ID, NOTIFICATION_CHANNEL_ID)
            .setChannelNameResourceId(R.string.playback_channel_name)
            .setChannelDescriptionResourceId(R.string.playback_channel_description)
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence =
                    player.currentMediaItem?.mediaMetadata?.title ?: "Redare Audio"

                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    val intent = packageManager.getLaunchIntentForPackage(packageName)?.setClass(this@PlaybackService, MainActivity::class.java)
                    return PendingIntent.getActivity(this@PlaybackService, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                }

                override fun getCurrentContentText(player: Player): CharSequence? =
                    player.currentMediaItem?.mediaMetadata?.artist

                override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): android.graphics.Bitmap? = null
            })
            .setSmallIconResourceId(R.drawable.ic_notification_radio)
            .build()

        notificationManager.setPlayer(player)
    }

    private fun saveCurrentProgress() {
        player.currentMediaItem?.let { mediaItem ->
            val chapterId = mediaItem.mediaId.toLongOrNull()
            if (chapterId != null && player.currentPosition > 0) {
                serviceScope.launch {
                    repository.savePlaybackPosition(chapterId, player.currentPosition)
                }
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        saveCurrentProgress()
        player.removeListener(playerListener)
        notificationManager.setPlayer(null)
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}