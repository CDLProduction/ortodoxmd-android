package md.ortodox.ortodoxmd.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import md.ortodox.ortodoxmd.data.dao.MonasteryDao
import md.ortodox.ortodoxmd.data.model.Monastery
import md.ortodox.ortodoxmd.data.network.MonasteryApiService
import javax.inject.Inject

class MonasteryRepository @Inject constructor(
    private val apiService: MonasteryApiService,
    private val dao: MonasteryDao
) {
    /**
     * Returnează un Flow cu lista de mănăstiri din cache.
     */
    fun getMonasteries(): Flow<List<Monastery>> = dao.getAll()

    /**
     * Sincronizează datele. Preia lista completă de la API și o înlocuiește în cache.
     */
    suspend fun syncMonasteries() {
        try {
            val monasteriesFromApi = apiService.getMonasteries()
            dao.sync(monasteriesFromApi)
        } catch (e: Exception) {
            Log.e("MonasteryRepository", "Failed to sync monasteries", e)
        }
    }
    fun getMonasteryById(id: Long): Flow<Monastery?> {
        return dao.getByIdFlow(id)
    }

}