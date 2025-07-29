package md.ortodox.ortodoxmd.data.network

import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookDto
import retrofit2.http.GET

interface AudiobookApiService {
    @GET("/api/audiobooks")
    suspend fun getAudiobooks(): List<AudiobookDto>
}