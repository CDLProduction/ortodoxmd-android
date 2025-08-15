package md.ortodox.ortodoxmd.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ImageLoaderModule {

    @Provides
    @Singleton
    fun provideImageLoader(@ApplicationContext context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    // Alocăm 25% din memoria RAM disponibilă aplicației pentru cache-ul de imagini.
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    // Creăm un folder 'image_cache' în directorul de cache al aplicației.
                    .directory(context.cacheDir.resolve("image_cache"))
                    // Alocăm 2% din spațiul de stocare disponibil pentru cache-ul de pe disc.
                    .maxSizePercent(0.02)
                    .build()
            }
            // Această opțiune este foarte importantă: îi spune lui Coil să ignore
            // headerele HTTP de la server și să salveze imaginile în cache oricum.
            .respectCacheHeaders(false)
            .build()
    }
}
