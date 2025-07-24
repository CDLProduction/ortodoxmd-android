package md.ortodox.ortodoxmd.data.repository

import md.ortodox.ortodoxmd.data.CalendarDao
import md.ortodox.ortodoxmd.data.network.CalendarApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import md.ortodox.ortodoxmd.data.PrayerDao
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
}