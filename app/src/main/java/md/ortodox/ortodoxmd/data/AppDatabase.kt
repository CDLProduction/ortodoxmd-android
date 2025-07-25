package md.ortodox.ortodoxmd.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import md.ortodox.ortodoxmd.data.model.CalendarData
import md.ortodox.ortodoxmd.data.model.Prayer
import md.ortodox.ortodoxmd.data.Converters

// Definește baza de date unică pentru calendar și rugăciuni
@Database(entities = [CalendarData::class, Prayer::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun calendarDao(): CalendarDao
    abstract fun prayerDao(): PrayerDao
}