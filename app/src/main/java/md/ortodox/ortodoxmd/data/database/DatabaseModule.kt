package md.ortodox.ortodoxmd.data.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import md.ortodox.ortodoxmd.data.dao.*

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "ortodox_calendar_db")
            .build()
    }

    @Provides
    fun provideCalendarDao(db: AppDatabase): CalendarDao = db.calendarDao()

    @Provides
    fun providePrayerDao(db: AppDatabase): PrayerDao = db.prayerDao()

    @Provides
    fun provideBibleDao(db: AppDatabase): BibleDao = db.bibleDao()

    @Provides
    fun provideAudiobookDao(db: AppDatabase): AudiobookDao = db.audiobookDao()
}