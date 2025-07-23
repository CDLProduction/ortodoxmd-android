package md.ortodox.ortodoxmd.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import md.ortodox.ortodoxmd.data.model.CalendarData

@Database(entities = [CalendarData::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun calendarDao(): CalendarDao
}