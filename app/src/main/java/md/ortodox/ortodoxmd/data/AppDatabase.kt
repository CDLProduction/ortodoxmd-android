package md.ortodox.ortodoxmd.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import md.ortodox.ortodoxmd.data.dao.BibleDao
import md.ortodox.ortodoxmd.data.dao.CalendarDao
import md.ortodox.ortodoxmd.data.model.CalendarData
import md.ortodox.ortodoxmd.data.model.Prayer
import md.ortodox.ortodoxmd.data.dao.PrayerDao
import md.ortodox.ortodoxmd.data.model.bible.BibleBook
import md.ortodox.ortodoxmd.data.model.bible.BibleBookmark
import md.ortodox.ortodoxmd.data.model.bible.BibleChapter
import md.ortodox.ortodoxmd.data.model.bible.BibleVerse

// Definește baza de date unică pentru calendar și rugăciuni
@Database(entities = [CalendarData::class, Prayer::class, BibleBook::class, BibleChapter::class, BibleVerse::class, BibleBookmark::class],
    version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun calendarDao(): CalendarDao
    abstract fun prayerDao(): PrayerDao
    abstract fun bibleDao(): BibleDao
}