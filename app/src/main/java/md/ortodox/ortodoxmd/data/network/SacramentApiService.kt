package md.ortodox.ortodoxmd.data.network

import md.ortodox.ortodoxmd.data.model.Sacrament
import retrofit2.http.GET

interface SacramentApiService {
    @GET("/api/sacraments")
    suspend fun getSacraments(): List<Sacrament>
}