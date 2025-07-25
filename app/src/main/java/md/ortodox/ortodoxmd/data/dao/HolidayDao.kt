package md.ortodox.ortodoxmd.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import md.ortodox.ortodoxmd.data.Holiday

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holidays WHERE date = :date")
    suspend fun getHolidayByDate(date: String): Holiday?

    @Insert
    suspend fun insertHoliday(holiday: Holiday)
}