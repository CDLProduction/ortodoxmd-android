package md.ortodox.ortodoxmd.data.repository

import md.ortodox.ortodoxmd.data.model.CalendarData
import md.ortodox.ortodoxmd.data.network.CalendarApiService
import javax.inject.Inject

class CalendarRepository @Inject constructor(
    private val apiService: CalendarApiService
) {
    suspend fun getCalendarData(date: String): CalendarData? {
        return try {
            apiService.getCalendarData(date, "en")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}