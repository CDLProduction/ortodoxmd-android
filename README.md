# OrtodoxMD Android

## Descriere Proiect
Acest repo conține clientul Android pentru OrtodoxMD, o aplicație mobilă dedicată conținutului ortodox pentru Mitropolia Moldovei, inspirată de aplicații similare ale Bisericii Ortodoxe Române. Aplicația folosește Kotlin, Jetpack Compose pentru UI, Room pentru stocare locală/offline, Retrofit pentru API calls la server, și ExoPlayer pentru media. Totul fără autentificare, cu acces imediat și opțiune de descărcare pentru offline. Proiectul este modular (MVVM), testabil, și compatibil Android 7.0+ (API 24-36). Open-source sub GPL v3, cu potențial pentru publicare pe Google Play Store.

Scop: Oferă interfață comodă pentru tineri, cu features ca calendar widget, radio streaming, biblie cu bookmark-uri.

Referințe cheie:
- [Android Developer Documentation](https://developer.android.com/docs)
- [Jetpack Compose Guide](https://developer.android.com/develop/ui/compose)

## Setup Inițial
1. Clonează repo-ul: `git clone https://github.com/yourusername/ortodoxmd-android.git`
2. Deschide în Android Studio (instalat via pacman).
3. Sync Gradle: Adaugă dependințe (Compose, Room, Retrofit, Hilt, ExoPlayer).
4. Pentru test pe device (ex: Pixel 8 Pro): Activează USB debugging, rulează `adb reverse tcp:8080 tcp:8080` pentru server local.
5. Run: Build și rulează pe emulator/device.

Dependințe cheie: minSdk 24, targetSdk 36.

## Checklist Features (Android-Side)
- [x] **Calendar Ortodox** - Ecran listă sărbători (fetch din server), widget home.
- [x] **Radio Online** - Streaming cu ExoPlayer, controale UI.
- [x] **Rugăciuni Ortodoxe** - Categorii, afișare text (offline cache în Room).
- [x] **Biblie** - Navigare capitole, bookmark local, descărcare offline.
- [ ] **Cărți Audio/Electronice** - Playback cu ExoPlayer, salvare poziție, download pentru offline.
- [ ] **Vieți Sfinți** - Listă biografii, imagini cu Coil.
- [ ] **Informații Icoane** - Galerie cu zoom, descrieri.
- [ ] **Hartă Interactivă** - Google Maps SDK cu markere locații (fetch JSON din server).
- [ ] **Slujbe Bisericești** - Detalii text, structurate.
- [ ] **Favorite și Liste** - Adăugare local (Room), listă "vreau să citesc/ascult".
- [x] **Dashboard** - Ecran home cu shortcuts la conținut frecvent (bazat pe acces local).
- [x] **Offline Support** - Verificare conexiune, cache/download cu DownloadManager/Room.(partial pentru functionalul actual)
- [ ] **Testing și CI** - Espresso UI tests, JUnit unit tests, GitHub Actions.
- [ ] **Deploy** - APK signing, pregătire Play Store (privacy policy).

Dezvoltare iterativă: Integrează features după server, test pe device real.
