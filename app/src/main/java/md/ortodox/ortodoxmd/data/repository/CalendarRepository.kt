package md.ortodox.ortodoxmd.data.repository

import md.ortodox.ortodoxmd.data.model.CalendarData
import md.ortodox.ortodoxmd.data.network.CalendarApiService
import md.ortodox.ortodoxmd.data.CalendarDao
import javax.inject.Inject
import android.util.Log
import androidx.room.Transaction

class CalendarRepository @Inject constructor(
    private val apiService: CalendarApiService,
    private val calendarDao: CalendarDao
) {
    suspend fun getCalendarData(date: String): CalendarData? {
        // Check cache first
        val cachedData = calendarDao.getCalendarDataByDate(date)
        Log.d("CalendarRepository", "Checked cache for date: $date - Found data: ${cachedData != null}")
        if (cachedData != null) {
            Log.d("CalendarRepository", "Returning cached data for date: $date")
            return cachedData
        }

        // Fetch from API if cache empty
        Log.d("CalendarRepository", "Cache empty - Fetching from API for date: $date")
        return try {
            val data = apiService.getCalendarData(date, "en")
            Log.d("CalendarRepository", "API returned data for date: $date")
            insertCalendarDataWithTransaction(data)
            Log.d("CalendarRepository", "Insertion completed successfully")
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