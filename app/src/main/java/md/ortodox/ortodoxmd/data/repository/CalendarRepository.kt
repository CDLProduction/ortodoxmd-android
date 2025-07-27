package md.ortodox.ortodoxmd.data.repository

import md.ortodox.ortodoxmd.data.model.CalendarData
import md.ortodox.ortodoxmd.data.network.CalendarApiService
import md.ortodox.ortodoxmd.data.dao.CalendarDao
import javax.inject.Inject
import android.util.Log
import androidx.room.Transaction

class CalendarRepository @Inject constructor(
    private val apiService: CalendarApiService,
    private val calendarDao: CalendarDao
) {
    suspend fun getCalendarData(date: String): CalendarData? {
        // Verificăm mai întâi în cache
        val cachedData = calendarDao.getCalendarDataByDate(date)
        if (cachedData != null) {
            return cachedData
        }

        // Dacă nu există în cache, preluăm de la API
        return try {
            // *** MODIFICARE APLICATĂ AICI ***
            // Am schimbat limba solicitată de la "en" la "ro"
            val data = apiService.getCalendarData(date, "ro")

            // Salvăm datele noi în cache
            insertCalendarDataWithTransaction(data)
            data
        } catch (e: Exception) {
            Log.e("CalendarRepository", "Error fetching or inserting calendar data: ${e.message}", e)
            null
        }
    }

    @Transaction
    suspend fun insertCalendarDataWithTransaction(data: CalendarData) {
        calendarDao.insertCalendarData(data)
    }
}
