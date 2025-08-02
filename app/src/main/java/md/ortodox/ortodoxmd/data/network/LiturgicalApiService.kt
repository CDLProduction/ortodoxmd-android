package md.ortodox.ortodoxmd.data.network

import md.ortodox.ortodoxmd.data.model.LiturgicalService
import retrofit2.http.GET
import retrofit2.http.Path

interface LiturgicalApiService {
    @GET("/api/liturgii/date/{date}")
    suspend fun getServicesByDate(@Path("date") date: String): List<LiturgicalService>
}