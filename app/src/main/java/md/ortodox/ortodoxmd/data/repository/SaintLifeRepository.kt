package md.ortodox.ortodoxmd.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import md.ortodox.ortodoxmd.data.dao.SaintLifeDao
import md.ortodox.ortodoxmd.data.network.SaintLifeApiService
import md.ortodox.ortodoxmd.data.model.SaintLife
import javax.inject.Inject

class SaintLifeRepository @Inject constructor(
    private val apiService: SaintLifeApiService,
    private val dao: SaintLifeDao
) {
    fun getSaintLives(): Flow<List<SaintLife>> = dao.getAll()

    suspend fun syncSaintLives() {
        try {
            val livesFromApi = apiService.getSaintLives()
            dao.insertAll(livesFromApi)
        } catch (e: Exception) {
            Log.e("SaintLifeRepository", "Failed to sync saint lives", e)
        }
    }

    suspend fun getById(id: Long): SaintLife? {
        return dao.getById(id) ?: try {
            val lifeFromServer = apiService.getSaintLifeDetails(id)
            dao.insertAll(listOf(lifeFromServer))
            lifeFromServer
        } catch (e: Exception) {
            null
        }
    }
}