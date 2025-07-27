package md.ortodox.ortodoxmd.ui.radio

/**
 * Un model simplu pentru a reține informațiile despre un post de radio.
 * @param name Numele postului de radio, care va fi afișat în UI.
 * @param streamUrl Link-ul direct către fluxul audio.
 * @param logoUrl (Opțional) Un link către o imagine-logo pentru postul de radio.
 */
data class RadioStation(
    val name: String,
    val streamUrl: String,
    val logoUrl: String? = null
)
