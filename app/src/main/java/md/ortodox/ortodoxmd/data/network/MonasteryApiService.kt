package md.ortodox.ortodoxmd.data.network

import md.ortodox.ortodoxmd.data.model.Monastery
import retrofit2.http.GET

interface MonasteryApiService {
    @GET("/api/monasteries")
    suspend fun getMonasteries(): List<Monastery>
}