package md.ortodox.ortodoxmd.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import md.ortodox.ortodoxmd.data.dao.ApologeticDao
import md.ortodox.ortodoxmd.data.model.Apologetic
import md.ortodox.ortodoxmd.data.network.ApologeticApiService
import javax.inject.Inject

class ApologeticRepository @Inject constructor(
    private val apiService: ApologeticApiService,
    private val dao: ApologeticDao
) {
    fun getApologetics(): Flow<List<Apologetic>> = dao.getAll()

    suspend fun syncApologetics() {
        try {
            val apologeticsFromApi = apiService.getApologetics()
            dao.sync(apologeticsFromApi)
        } catch (e: Exception) {
            Log.e("ApologeticRepository", "Failed to sync apologetics", e)
        }
    }
}