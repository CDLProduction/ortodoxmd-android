package md.ortodox.ortodoxmd.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import md.ortodox.ortodoxmd.data.model.Prayer


@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayers WHERE category = :category")
    suspend fun getPrayersByCategory(category: String): List<Prayer>

    @Upsert
    suspend fun insertPrayers(prayers: List<Prayer>)
}