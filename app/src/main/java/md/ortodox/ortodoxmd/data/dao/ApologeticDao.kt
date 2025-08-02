package md.ortodox.ortodoxmd.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import md.ortodox.ortodoxmd.data.model.Apologetic

@Dao
interface ApologeticDao {
    @Query("SELECT * FROM apologetics ORDER BY category, id ASC")
    fun getAll(): Flow<List<Apologetic>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apologetics: List<Apologetic>)

    @Query("DELETE FROM apologetics")
    suspend fun clearAll()

    @Transaction
    suspend fun sync(apologetics: List<Apologetic>) {
        clearAll()
        insertAll(apologetics)
    }
}