package md.ortodox.ortodoxmd.data.repository

import md.ortodox.ortodoxmd.data.CalendarDao
import md.ortodox.ortodoxmd.data.model.CalendarData
import md.ortodox.ortodoxmd.data.network.CalendarApiService
import javax.inject.Inject

class CalendarRepository @Inject constructor(
    private val apiService: CalendarApiService,
    private val calendarDao: CalendarDao
) {
    suspend fun getCalendarData(date: String): CalendarData? {
        return try {
            val cachedData = calendarDao.getCalendarDataByDate(date)
            cachedData ?: run {
                val data = apiService.getCalendarData(date, "en")
                calendarDao.insertCalendarData(data)
                data
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}