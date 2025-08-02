package md.ortodox.ortodoxmd.data.network

import md.ortodox.ortodoxmd.data.model.Apologetic
import retrofit2.http.GET

interface ApologeticApiService {
    @GET("/api/apologetics")
    suspend fun getApologetics(): List<Apologetic>
}