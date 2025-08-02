package md.ortodox.ortodoxmd.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import md.ortodox.ortodoxmd.data.model.SaintLife

@Dao
interface SaintLifeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(saintLives: List<SaintLife>)

    @Query("SELECT * FROM saint_lives ORDER BY name_ro ASC")
    fun getAll(): Flow<List<SaintLife>>

    @Query("SELECT * FROM saint_lives WHERE id = :id")
    suspend fun getById(id: Long): SaintLife?
}