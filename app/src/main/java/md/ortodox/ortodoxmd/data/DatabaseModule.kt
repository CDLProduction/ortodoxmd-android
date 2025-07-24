package md.ortodox.ortodoxmd.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "ortodox_calendar_db"
            ).addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Log.d("AppDatabase", "Database created successfully")
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    Log.d("AppDatabase", "Database opened")
                }
            }).fallbackToDestructiveMigration(true)  // Resetează DB la schimbări (pentru dezvoltare)
            .build()
    }

    @Provides
    fun provideCalendarDao(database: AppDatabase): CalendarDao {
        return database.calendarDao()
    }

    @Provides
    fun providePrayerDao(database: AppDatabase): PrayerDao {
        return database.prayerDao()
    }
}