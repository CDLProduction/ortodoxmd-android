// Fișier modificat: NetworkModule.kt (adaugă logică pentru emulator vs device real)
package md.ortodox.ortodoxmd.data.network

import android.os.Build
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

    // FIX: Detectează dacă rulează pe emulator sau device real
    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT
    }

    // FIX: Ajustează BASE_URL în funcție de mediu
    // Pe emulator: Folosește 10.0.2.2 (alias pentru localhost-ul host-ului)
    // Pe device real: Folosește 127.0.0.1 cu ADB reverse, sau schimbă la IP-ul host-ului (ex: "http://192.168.1.100:8080/" – ajustează după rețeaua ta)
    val BASE_URL_MAIN = if (isEmulator()) "http://10.0.2.2:8080/" else "http://127.0.0.1:8080/"
    val BASE_URL_AUDIOBOOKS = if (isEmulator()) "http://10.0.2.2:8081/" else "http://127.0.0.1:8081/"

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
            .baseUrl(BASE_URL_MAIN)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("AudiobookRetrofit")
    fun provideAudiobookRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL_AUDIOBOOKS)
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

    // Înlocuiește provideSaintApiService cu provideSaintLifeApiService
    @Provides
    @Singleton
    fun provideSaintLifeApiService(retrofit: Retrofit): SaintLifeApiService = retrofit.create(SaintLifeApiService::class.java)

    @Provides
    @Singleton
    fun provideIconApiService(@Named("AudiobookRetrofit") retrofit: Retrofit): IconApiService = retrofit.create(IconApiService::class.java)


    @Provides
    @Singleton
    fun provideLiturgicalApiService(retrofit: Retrofit): LiturgicalApiService = retrofit.create(LiturgicalApiService::class.java)

    @Provides
    @Singleton
    fun provideMonasteryApiService(retrofit: Retrofit): MonasteryApiService = retrofit.create(MonasteryApiService::class.java)

    @Provides
    @Singleton
    fun provideSacramentApiService(retrofit: Retrofit): SacramentApiService = retrofit.create(SacramentApiService::class.java)

    @Provides
    @Singleton
    fun provideApologeticApiService(retrofit: Retrofit): ApologeticApiService = retrofit.create(ApologeticApiService::class.java)

}