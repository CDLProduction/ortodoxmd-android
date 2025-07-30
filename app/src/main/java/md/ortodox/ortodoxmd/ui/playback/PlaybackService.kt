package md.ortodox.ortodoxmd.ui.playback

import android.app.PendingIntent
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
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
@AndroidEntryPoint // NOU
class PlaybackService : MediaSessionService() {

    @Inject // NOU
    lateinit var repository: AudiobookRepository

    @Inject // NOU
    lateinit var player: ExoPlayer

    private var mediaSession: MediaSession? = null
    private lateinit var notificationManager: PlayerNotificationManager
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            // Salvăm progresul de fiecare dată când utilizatorul apasă Pauză
            if (!isPlaying) {
                saveCurrentProgress()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        player.addListener(playerListener)
        mediaSession = MediaSession.Builder(this, player).build()

        // Configurare Notificare (rămâne la fel)
        notificationManager = PlayerNotificationManager.Builder(this, NOTIFICATION_ID, NOTIFICATION_CHANNEL_ID)
            .setChannelNameResourceId(R.string.playback_channel_name)
            .setChannelDescriptionResourceId(R.string.playback_channel_description)
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    return player.currentMediaItem?.mediaMetadata?.title ?: "Redare Audio"
                }
                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    val intent = packageManager.getLaunchIntentForPackage(packageName)?.setClass(this@PlaybackService, MainActivity::class.java)
                    return PendingIntent.getActivity(this@PlaybackService, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                }
                override fun getCurrentContentText(player: Player): CharSequence? {
                    return player.currentMediaItem?.mediaMetadata?.artist
                }
                override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): android.graphics.Bitmap? = null
            })
            .setSmallIconResourceId(R.drawable.ic_notification_radio)
            .build()

        notificationManager.setPlayer(player)
        mediaSession?.let { notificationManager.setMediaSessionToken(it.sessionCompatToken) }
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
        saveCurrentProgress() // O ultimă salvare
        player.removeListener(playerListener)
        notificationManager.setPlayer(null)
        mediaSession?.run {
            // Player-ul este Singleton, nu îl eliberăm aici, ci lăsăm Hilt să gestioneze
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}