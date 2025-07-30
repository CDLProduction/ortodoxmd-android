package md.ortodox.ortodoxmd.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import md.ortodox.ortodoxmd.data.converter.Converters
import md.ortodox.ortodoxmd.data.dao.*
import md.ortodox.ortodoxmd.data.model.*
import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity
import md.ortodox.ortodoxmd.data.model.audiobook.LastPlayback
import md.ortodox.ortodoxmd.data.model.bible.*

@Database(entities = [
    CalendarData::class, Prayer::class, BibleBook::class, BibleChapter::class,
    BibleVerse::class, BibleBookmark::class, AudiobookEntity::class,
    BibleTestament::class, LastPlayback::class
], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun calendarDao(): CalendarDao
    abstract fun prayerDao(): PrayerDao
    abstract fun bibleDao(): BibleDao
    abstract fun audiobookDao(): AudiobookDao
}