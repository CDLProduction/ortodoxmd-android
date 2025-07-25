package md.ortodox.ortodoxmd.data.network

import md.ortodox.ortodoxmd.data.model.Prayer
import retrofit2.http.GET
import retrofit2.http.Query

interface PrayerApiService {
    @GET("api/prayers")
    suspend fun getPrayersByCategory(@Query("category") category: String): List<Prayer>
}