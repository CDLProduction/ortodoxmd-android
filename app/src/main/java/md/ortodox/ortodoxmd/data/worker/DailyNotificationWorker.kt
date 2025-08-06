package md.ortodox.ortodoxmd.data.worker

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import md.ortodox.ortodoxmd.notifications.NotificationHelper
import md.ortodox.ortodoxmd.data.repository.CalendarRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@HiltWorker
class DailyNotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val calendarRepository: CalendarRepository
) : CoroutineWorker(context, workerParameters) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        return try {
            val dateString = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val calendarData = calendarRepository.getCalendarData(dateString)

            if (calendarData != null) {
                val title = "Astăzi, ${LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM", Locale("ro")))}"

                val fastingInfo = when (calendarData.fastingDescriptionRo.lowercase(Locale.ROOT)) {
                    "harti" -> "Zi fără post (Harți)"
                    else -> calendarData.fastingDescriptionRo
                }

                val saintsInfo = calendarData.saints
                    .take(2) // Luăm doar primii 2 sfinți pentru a nu face notificarea prea lungă
                    .joinToString { it.nameAndDescriptionRo }

                val content = "Post: $fastingInfo.\nSfinți: $saintsInfo"

                NotificationHelper.showDailyNotification(context, title, content)
            }
            Result.success()
        } catch (e: Exception) {
            // Dacă apare o eroare (ex: fără internet), reîncearcă mai târziu
            Result.retry()
        }
    }
}