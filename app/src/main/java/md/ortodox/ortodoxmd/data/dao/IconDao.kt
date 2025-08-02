package md.ortodox.ortodoxmd.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import md.ortodox.ortodoxmd.data.model.Icon

@Dao
interface IconDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(icons: List<Icon>)

    @Query("SELECT * FROM icons")
    fun getAll(): Flow<List<Icon>>

    @Query("SELECT * FROM icons WHERE id = :id")
    suspend fun getById(id: Long): Icon?
}