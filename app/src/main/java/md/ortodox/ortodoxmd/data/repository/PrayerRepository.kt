package md.ortodox.ortodoxmd.data.repository

import md.ortodox.ortodoxmd.data.model.Prayer
import md.ortodox.ortodoxmd.data.network.PrayerApiService
import md.ortodox.ortodoxmd.data.dao.PrayerDao
import javax.inject.Inject
import android.util.Log
import java.util.Locale
import androidx.room.Transaction


class PrayerRepository @Inject constructor(
    private val apiService: PrayerApiService,
    private val prayerDao: PrayerDao
) {
    suspend fun getPrayersByCategory(category: String): List<Prayer>? {
        val normalizedCategory = category.uppercase(Locale.ROOT)
        // Check cache first
        val cachedPrayers = prayerDao.getPrayersByCategory(normalizedCategory)
        Log.d("PrayerRepository", "Checked cache for normalized category: $normalizedCategory - Found ${cachedPrayers.size} items")
        if (cachedPrayers.isNotEmpty()) {
            Log.d("PrayerRepository", "Returning cached prayers for category: $normalizedCategory")
            return cachedPrayers
        }

        // Fetch from API if cache empty
        Log.d("PrayerRepository", "Cache empty - Fetching from API for category: $category")
        return try {
            val data = apiService.getPrayersByCategory(category)
            Log.d("PrayerRepository", "API returned ${data.size} prayers for category: $category")
            if (data.isNotEmpty()) {
                Log.d("PrayerRepository", "Normalizing and caching prayers for normalized category: $normalizedCategory")
                val normalizedData = normalizePrayers(data, normalizedCategory)
                prayerDao.insertPrayers(normalizedData)
                Log.d("PrayerRepository", "Insertion completed successfully")
                normalizedData
            } else {
                Log.w("PrayerRepository", "API returned empty data - No insertion")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("PrayerRepository", "Error fetching or inserting prayers: ${e.message}", e)
            null
        }
    }

    private fun normalizePrayers(prayers: List<Prayer>, normalizedCategory: String): List<Prayer> {
        return prayers.map { prayer ->
            prayer.copy(
                category = normalizedCategory,
                subPrayers = normalizePrayers(prayer.subPrayers, normalizedCategory)  // Recursiv pentru subPrayers
            )
        }
    }

    @Transaction
    suspend fun insertPrayersWithTransaction(prayers: List<Prayer>) {
        prayerDao.insertPrayers(prayers)
    }
}