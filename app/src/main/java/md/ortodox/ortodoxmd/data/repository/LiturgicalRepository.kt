package md.ortodox.ortodoxmd.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import md.ortodox.ortodoxmd.data.dao.LiturgicalServiceDao
import md.ortodox.ortodoxmd.data.model.LiturgicalService
import md.ortodox.ortodoxmd.data.network.LiturgicalApiService
import javax.inject.Inject

class LiturgicalRepository @Inject constructor(
    private val apiService: LiturgicalApiService,
    private val dao: LiturgicalServiceDao
) {
    /**
     * Returnează un Flow cu datele din cache. UI-ul va observa acest Flow.
     */
    fun getServicesByDate(date: String): Flow<List<LiturgicalService>> {
        return dao.getServicesByDate(date)
    }

    /**
     * Sincronizează datele pentru o anumită zi.
     * Preia datele de la API și le salvează în baza de date locală.
     */
    suspend fun syncServicesForDate(date: String) {
        try {
            val servicesFromApi = apiService.getServicesByDate(date)
            // Folosim funcția tranzacțională pentru a șterge datele vechi și a insera cele noi
            dao.updateServicesForDate(date, servicesFromApi)
        } catch (e: Exception) {
            Log.e("LiturgicalRepository", "Failed to sync services for date $date", e)
        }
    }
}