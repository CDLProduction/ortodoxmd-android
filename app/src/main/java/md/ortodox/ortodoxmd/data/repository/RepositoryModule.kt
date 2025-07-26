package md.ortodox.ortodoxmd.data.repository

import md.ortodox.ortodoxmd.data.dao.CalendarDao
import md.ortodox.ortodoxmd.data.network.CalendarApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import md.ortodox.ortodoxmd.data.dao.BibleDao
import md.ortodox.ortodoxmd.data.dao.PrayerDao
import md.ortodox.ortodoxmd.data.network.BibleApiService
import md.ortodox.ortodoxmd.data.network.PrayerApiService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideCalendarRepository(
        apiService: CalendarApiService,
        calendarDao: CalendarDao
    ): CalendarRepository {
        return CalendarRepository(apiService, calendarDao)
    }

    @Provides
    @Singleton
    fun providePrayerRepository(
        apiService: PrayerApiService,
        prayerDao: PrayerDao
    ): PrayerRepository {
        return PrayerRepository(apiService, prayerDao)
    }

    @Provides
    @Singleton
    fun provideBibleRepository(
        apiService: BibleApiService,
        bibleDao: BibleDao
    ): BibleRepository {
        return BibleRepository(apiService, bibleDao)
    }
}