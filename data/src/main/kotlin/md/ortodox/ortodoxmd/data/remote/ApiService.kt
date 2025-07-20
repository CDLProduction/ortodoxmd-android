package md.ortodox.ortodoxmd.data.remote

import md.ortodox.ortodoxmd.data.model.CalendarResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/api/calendar/{date}")
    suspend fun getCalendar(
        @Path("date") date: String,  // ex: "2025-07-19"
        @Query("lang") lang: String = "en"  // Limba, default en
    ): CalendarResponse
}