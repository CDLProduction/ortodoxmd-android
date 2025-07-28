package md.ortodox.ortodoxmd.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import md.ortodox.ortodoxmd.data.dao.*
import md.ortodox.ortodoxmd.data.model.*
import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity
import md.ortodox.ortodoxmd.data.model.bible.*

@Database(entities = [
    CalendarData::class, Prayer::class, BibleBook::class, BibleChapter::class,
    BibleVerse::class, BibleBookmark::class, AudiobookEntity::class,
    BibleTestament::class // *** CORECȚIE APLICATĂ AICI: Am adăugat entitatea lipsă ***
], version = 3, exportSchema = false) // Asigură-te că versiunea este incrementată dacă faci modificări
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun calendarDao(): CalendarDao
    abstract fun prayerDao(): PrayerDao
    abstract fun bibleDao(): BibleDao
    abstract fun audiobookDao(): AudiobookDao
}
