package md.ortodox.ortodoxmd

import android.app.Application
import android.content.Context
import android.content.res.Configuration
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
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class OrtodoxMDApplication : Application() {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var languageManager: LanguageManager

    override fun attachBaseContext(base: Context) {
        // Creăm o instanță manuală de LanguageManager pentru a citi limba.
        val tempLanguageManager = LanguageManager(base)
        val lang = tempLanguageManager.getCurrentLanguageSync()

        // NOU și ESENȚIAL: Folosim AppCompatDelegate pentru a seta limba la nivel de proces.
        // Aceasta este metoda modernă și recomandată care persistă setarea.
        val localeList = LocaleListCompat.forLanguageTags(lang)
        AppCompatDelegate.setApplicationLocales(localeList)

        // Păstrăm și logica de creare a unui context nou pentru compatibilitate maximă.
        val locale = Locale(lang)
        val config = Configuration(base.resources.configuration)
        @Suppress("DEPRECATION")
        config.setLocale(locale)
        val newContext = base.createConfigurationContext(config)

        super.attachBaseContext(newContext)
    }

    override fun onCreate() {
        super.onCreate()

        // Folosim calea completă pentru a evita coliziunea de nume
        val workManagerConfig = androidx.work.Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()
        WorkManager.initialize(this, workManagerConfig)

        Log.d("OrtodoxMDApplication", "Hilt & WorkManager initialized.")

        // Logica pentru notificări rămâne neschimbată
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
