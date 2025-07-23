package md.ortodox.ortodoxmd.data.repository

import md.ortodox.ortodoxmd.data.network.CalendarApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideCalendarRepository(apiService: CalendarApiService): CalendarRepository {
        return CalendarRepository(apiService)
    }
}