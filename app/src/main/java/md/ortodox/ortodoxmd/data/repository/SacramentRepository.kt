package md.ortodox.ortodoxmd.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import md.ortodox.ortodoxmd.data.dao.SacramentDao
import md.ortodox.ortodoxmd.data.model.Sacrament
import md.ortodox.ortodoxmd.data.network.SacramentApiService
import javax.inject.Inject

class SacramentRepository @Inject constructor(
    private val apiService: SacramentApiService,
    private val dao: SacramentDao
) {
    fun getSacraments(): Flow<List<Sacrament>> = dao.getAll()

    suspend fun syncSacraments() {
        try {
            val sacramentsFromApi = apiService.getSacraments()
            dao.sync(sacramentsFromApi)
        } catch (e: Exception) {
            Log.e("SacramentRepository", "Failed to sync sacraments", e)
        }
    }
}