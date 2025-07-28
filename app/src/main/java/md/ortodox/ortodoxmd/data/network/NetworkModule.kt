package md.ortodox.ortodoxmd.data.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        return OkHttpClient.Builder().addInterceptor(logging).build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
//            .baseUrl("http://10.0.2.2:8080/") // adresa de conectare emulator
            .baseUrl("http://127.0.0.1:8080/") // adresa de conectare dispozitiv real usb port forward
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("AudiobookRetrofit")
    fun provideAudiobookRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
//            .baseUrl("http://10.0.2.2:8081/") // adresa de conectare emulator
            .baseUrl("http://127.0.0.1:8081/")  // adresa de conectare dispozitiv real usb port forward
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideCalendarApiService(retrofit: Retrofit): CalendarApiService = retrofit.create(CalendarApiService::class.java)

    @Provides
    @Singleton
    fun providePrayerApiService(retrofit: Retrofit): PrayerApiService = retrofit.create(PrayerApiService::class.java)

    @Provides
    @Singleton
    fun provideBibleApiService(retrofit: Retrofit): BibleApiService = retrofit.create(BibleApiService::class.java)

    @Provides
    @Singleton
    fun provideAudiobookApiService(@Named("AudiobookRetrofit") retrofit: Retrofit): AudiobookApiService = retrofit.create(AudiobookApiService::class.java)
}
