package md.ortodox.ortodoxmd.ui.playback

import android.app.PendingIntent
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import md.ortodox.ortodoxmd.MainActivity
import md.ortodox.ortodoxmd.R

private const val NOTIFICATION_ID = 101
private const val NOTIFICATION_CHANNEL_ID = "ortodoxmd_playback_channel"

@UnstableApi
class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private lateinit var notificationManager: PlayerNotificationManager

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()

        notificationManager = PlayerNotificationManager.Builder(this, NOTIFICATION_ID, NOTIFICATION_CHANNEL_ID)
            .setChannelNameResourceId(R.string.playback_channel_name)
            .setChannelDescriptionResourceId(R.string.playback_channel_description)
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: androidx.media3.common.Player): CharSequence {
                    return player.currentMediaItem?.mediaMetadata?.title ?: "Redare Audio"
                }
                override fun createCurrentContentIntent(player: androidx.media3.common.Player): PendingIntent? {
                    val intent = packageManager.getLaunchIntentForPackage(packageName)?.setClass(
                        this@PlaybackService,
                        MainActivity::class.java
                    )
                    return PendingIntent.getActivity(
                        this@PlaybackService, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                }
                override fun getCurrentContentText(player: androidx.media3.common.Player): CharSequence? {
                    return player.currentMediaItem?.mediaMetadata?.artist
                }
                override fun getCurrentLargeIcon(
                    player: androidx.media3.common.Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): android.graphics.Bitmap? = null
            })
            .setSmallIconResourceId(R.drawable.ic_notification_radio)
            .build()

        notificationManager.setPlayer(player)

        // *** CORECȚIE APLICATĂ AICI ***
        // Folosim un bloc 'let' pentru a ne asigura că 'mediaSession' nu este null
        // și pentru a pasa un token non-nullable metodei.
        mediaSession?.let { session ->
            notificationManager.setMediaSessionToken(session.sessionCompatToken)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        notificationManager.setPlayer(null)
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
