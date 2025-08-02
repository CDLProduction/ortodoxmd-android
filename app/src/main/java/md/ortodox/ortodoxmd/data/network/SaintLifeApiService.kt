package md.ortodox.ortodoxmd.data.network

import md.ortodox.ortodoxmd.data.model.SaintLife
import retrofit2.http.GET
import retrofit2.http.Path

interface SaintLifeApiService {
    @GET("/api/saint-lives")
    suspend fun getSaintLives(): List<SaintLife>

    @GET("/api/saint-lives/{id}")
    suspend fun getSaintLifeDetails(@Path("id") id: Long): SaintLife
}