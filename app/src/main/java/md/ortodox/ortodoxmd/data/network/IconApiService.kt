package md.ortodox.ortodoxmd.data.network

import md.ortodox.ortodoxmd.data.model.Icon
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

interface IconApiService {

    @GET("/api/icons")
    suspend  fun getIcons(): List<Icon>

    @Streaming
    @GET("/api/icons/{id}/stream")
    suspend fun streamIcon(@Path("id") id: Long): ResponseBody
}