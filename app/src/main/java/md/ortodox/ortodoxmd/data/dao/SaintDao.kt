package md.ortodox.ortodoxmd.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import md.ortodox.ortodoxmd.data.model.Saint

@Dao
interface SaintDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(saints: List<Saint>)

    @Query("SELECT * FROM saints")
    fun getAll(): Flow<List<Saint>>

    @Query("SELECT * FROM saints WHERE id = :id")
    suspend fun getById(id: Long): Saint?
}