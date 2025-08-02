package md.ortodox.ortodoxmd.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import md.ortodox.ortodoxmd.data.model.Sacrament

@Dao
interface SacramentDao {
    @Query("SELECT * FROM sacraments ORDER BY category, id ASC")
    fun getAll(): Flow<List<Sacrament>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sacraments: List<Sacrament>)

    @Query("DELETE FROM sacraments")
    suspend fun clearAll()

    @Transaction
    suspend fun sync(sacraments: List<Sacrament>) {
        clearAll()
        insertAll(sacraments)
    }
}