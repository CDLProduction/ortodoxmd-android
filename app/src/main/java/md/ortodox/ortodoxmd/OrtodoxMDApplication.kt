package md.ortodox.ortodoxmd

import android.app.Application
import android.icu.util.Calendar
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import md.ortodox.ortodoxmd.data.worker.DailyNotificationWorker
import java.util.concurrent.TimeUnit
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

    private fun scheduleDailyNotification() {
        val workRequest = PeriodicWorkRequestBuilder<DailyNotificationWorker>(1, TimeUnit.DAYS)
            // Setează constrângeri: worker-ul va rula doar când există conexiune la internet
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            // Setează o întârziere inițială pentru a rula prima dată la ora 7 AM
            .setInitialDelay(calculateInitialDelayTo7AM(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyCalendarNotification",
            ExistingPeriodicWorkPolicy.KEEP, // Dacă task-ul e deja programat, nu îl înlocui
            workRequest
        )
    }

    // Calculează timpul rămas până la ora 7 AM a zilei curente sau următoare
    private fun calculateInitialDelayTo7AM(): Long {
        val now = Calendar.getInstance()
        val nextRun = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            // Dacă ora 7 AM a trecut deja astăzi, programează pentru mâine
            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return nextRun.timeInMillis - now.timeInMillis
    }
}
