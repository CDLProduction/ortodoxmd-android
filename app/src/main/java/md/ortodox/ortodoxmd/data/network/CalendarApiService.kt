package md.ortodox.ortodoxmd.data.network

import md.ortodox.ortodoxmd.data.model.CalendarData
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CalendarApiService {
    @GET("api/calendar/{date}")
    suspend fun getCalendarData(
        @Path("date") date: String,
        @Query("lang") lang: String = "en"
    ): CalendarData
}