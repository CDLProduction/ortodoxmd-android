package md.ortodox.ortodoxmd.data.repository

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import md.ortodox.ortodoxmd.data.local.CalendarDao
import md.ortodox.ortodoxmd.data.local.CalendarEntity
import md.ortodox.ortodoxmd.data.model.CalendarResponse
import md.ortodox.ortodoxmd.data.remote.ApiService
import javax.inject.Inject

class CalendarRepository @Inject constructor(
    private val apiService: ApiService,
    private val calendarDao: CalendarDao,
    @ApplicationContext private val context: Context
) {
    suspend fun getCalendar(date: String, lang: String = "en"): CalendarEntity = withContext(Dispatchers.IO) {
        if (isOnline(context)) {
            try {
                val response = apiService.getCalendar(date, lang)
                val entity = response.toEntity()  // Creează extension pentru mapping
                calendarDao.insert(entity)
                entity
            } catch (e: Exception) {
                calendarDao.getByDate(date) ?: throw e  // Fallback local sau error
            }
        } else {
            calendarDao.getByDate(date) ?: throw NoSuchElementException("No data offline for $date")
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
}

// Extension pentru mapping Response to Entity
fun CalendarResponse.toEntity(): CalendarEntity = CalendarEntity(
    date = date,
    fastingType = fastingType,
    fastingDescriptionEn = fastingDescriptionEn,
    fastingDescriptionRo = fastingDescriptionRo,
    fastingDescriptionRu = fastingDescriptionRu,
    summaryTitleEn = summaryTitleEn,
    summaryTitleRo = summaryTitleRo,
    summaryTitleRu = summaryTitleRu,
    titlesEn = titlesEn,
    titlesRo = titlesRo,
    titlesRu = titlesRu,
    saints = saints,
    fastingDay = fastingDay
)