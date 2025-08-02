package md.ortodox.ortodoxmd.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import md.ortodox.ortodoxmd.data.model.LiturgicalService

@Dao
interface LiturgicalServiceDao {

    @Query("SELECT * FROM liturgical_services WHERE calendar_date = :date ORDER BY id ASC")
    fun getServicesByDate(date: String): Flow<List<LiturgicalService>>

    @Query("DELETE FROM liturgical_services WHERE calendar_date = :date")
    suspend fun deleteServicesByDate(date: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(services: List<LiturgicalService>)

    // O funcție tranzacțională pentru a asigura integritatea datelor la update
    @Transaction
    suspend fun updateServicesForDate(date: String, services: List<LiturgicalService>) {
        deleteServicesByDate(date)
        insertAll(services)
    }
}