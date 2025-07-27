package md.ortodox.ortodoxmd.ui.radio

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Acesta este serviciul care rulează în fundal și gestionează redarea audio.
 * Este o componentă esențială pentru ca radioul să funcționeze și când aplicația este închisă.
 */
class RadioService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    // Se apelează o singură dată la crearea serviciului
    override fun onCreate() {
        super.onCreate()
        // Inițializăm player-ul și sesiunea media
        val player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()
    }

    // Returnează sesiunea media pentru a permite controlul din alte părți ale aplicației
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    // Se apelează la distrugerea serviciului pentru a elibera resursele
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
