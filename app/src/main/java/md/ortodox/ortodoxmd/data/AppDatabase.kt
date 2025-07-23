package md.ortodox.ortodoxmd.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Holiday::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun holidayDao(): HolidayDao
}