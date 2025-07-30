package md.ortodox.ortodoxmd

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class OrtodoxMDApplication : Application() {

    // Injectăm direct HiltWorkerFactory. Hilt se va ocupa de crearea ei.
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        // Creăm configurația noastră personalizată, care știe despre Hilt.
        val config = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()

        // Inițializăm manual WorkManager cu configurația noastră.
        // Acest pas funcționează deoarece am dezactivat inițializarea automată în manifest.
        WorkManager.initialize(this, config)

        Log.d("AudiobookDownload", "[Application] Hilt & WorkManager initialized manually.")
    }
}
