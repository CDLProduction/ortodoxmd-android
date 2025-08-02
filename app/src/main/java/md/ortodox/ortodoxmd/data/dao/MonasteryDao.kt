package md.ortodox.ortodoxmd.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import md.ortodox.ortodoxmd.data.model.Monastery

@Dao
interface MonasteryDao {
    @Query("SELECT * FROM monasteries ORDER BY name_ro ASC")
    fun getAll(): Flow<List<Monastery>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(monasteries: List<Monastery>)

    @Query("DELETE FROM monasteries")
    suspend fun clearAll()

    @Transaction
    suspend fun sync(monasteries: List<Monastery>) {
        clearAll()
        insertAll(monasteries)
    }
    @Query("SELECT * FROM monasteries WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<Monastery?>
}