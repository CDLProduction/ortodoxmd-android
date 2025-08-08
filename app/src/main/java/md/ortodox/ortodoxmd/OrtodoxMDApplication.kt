package md.ortodox.ortodoxmd

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import md.ortodox.ortodoxmd.data.language.LanguageManager
import md.ortodox.ortodoxmd.data.worker.DailyNotificationWorker
import md.ortodox.ortodoxmd.notifications.NotificationHelper
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class OrtodoxMDApplication : Application() {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    @Inject
    lateinit var languageManager: LanguageManager

    override fun attachBaseContext(base: Context) {
        // Create a temporary LanguageManager to read the saved language
        val tempLanguageManager = LanguageManager(base)
        val lang = tempLanguageManager.getCurrentLanguageSync()
        val localeList = LocaleListCompat.forLanguageTags(lang)
        AppCompatDelegate.setApplicationLocales(localeList)
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        val workManagerConfig = androidx.work.Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()
        WorkManager.initialize(this, workManagerConfig)
        Log.d("OrtodoxMDApplication", "Hilt & WorkManager initialized.")
        NotificationHelper.createNotificationChannel(this)
        scheduleDailyNotification()
    }

    private fun scheduleDailyNotification() {
        val workRequest = PeriodicWorkRequestBuilder<DailyNotificationWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInitialDelay(calculateInitialDelayTo7AM(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyCalendarNotification",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun calculateInitialDelayTo7AM(): Long {
        val now = Calendar.getInstance()
        val nextRun = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return nextRun.timeInMillis - now.timeInMillis
    }
}