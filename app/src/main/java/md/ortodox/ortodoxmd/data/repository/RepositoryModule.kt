package md.ortodox.ortodoxmd.data.repository

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import md.ortodox.ortodoxmd.data.dao.*
import md.ortodox.ortodoxmd.data.network.*
import md.ortodox.ortodoxmd.data.network.AudiobookApiService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideCalendarRepository(api: CalendarApiService, dao: CalendarDao) = CalendarRepository(api, dao)

    @Provides
    @Singleton
    fun providePrayerRepository(api: PrayerApiService, dao: PrayerDao) = PrayerRepository(api, dao)

    @Provides
    @Singleton
    fun provideBibleRepository(api: BibleApiService, dao: BibleDao) = BibleRepository(api, dao)

    @Provides
    @Singleton
    fun provideAudiobookRepository(
        apiService: AudiobookApiService,
        audiobookDao: AudiobookDao,
        @ApplicationContext context: Context
    ): AudiobookRepository {
        return AudiobookRepository(apiService, audiobookDao, context)
    }

    @Provides
    @Singleton
    fun provideSaintLifeRepository(
        apiService: SaintLifeApiService,
        dao: SaintLifeDao
    ): SaintLifeRepository {
        return SaintLifeRepository(apiService, dao)
    }

    @Provides
    @Singleton
    fun provideIconRepository(
        apiService: IconApiService,
        iconDao: IconDao
    ): IconRepository {
        return IconRepository(apiService, iconDao)
    }


    @Provides
    @Singleton
    fun provideLiturgicalRepository(api: LiturgicalApiService, dao: LiturgicalServiceDao) = LiturgicalRepository(api, dao)

    @Provides
    @Singleton
    fun provideMonasteryRepository(api: MonasteryApiService, dao: MonasteryDao) = MonasteryRepository(api, dao)

    @Provides
    @Singleton
    fun provideSacramentRepository(api: SacramentApiService, dao: SacramentDao) = SacramentRepository(api, dao)

}
