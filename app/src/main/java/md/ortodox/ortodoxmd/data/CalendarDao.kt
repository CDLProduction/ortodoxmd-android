package md.ortodox.ortodoxmd.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import md.ortodox.ortodoxmd.data.model.CalendarData

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_data WHERE date = :date")
    suspend fun getCalendarDataByDate(date: String): CalendarData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarData(data: CalendarData)
}