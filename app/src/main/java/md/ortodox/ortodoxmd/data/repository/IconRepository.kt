package md.ortodox.ortodoxmd.data.repository

import kotlinx.coroutines.flow.Flow
import md.ortodox.ortodoxmd.data.dao.IconDao
import md.ortodox.ortodoxmd.data.network.IconApiService
import md.ortodox.ortodoxmd.data.model.Icon
import javax.inject.Inject

class IconRepository @Inject constructor(
    private val apiService: IconApiService,
    private val iconDao: IconDao
) {
    fun getIcons(): Flow<List<Icon>> = iconDao.getAll()

    suspend fun syncIcons() {
        try {
            val icons = apiService.getIcons()
            iconDao.insertAll(icons)
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun getIconStream(id: Long): ByteArray? = try {
        apiService.streamIcon(id).bytes()
    } catch (e: Exception) {
        null
    }

    suspend fun getById(id: Long): Icon? = iconDao.getById(id) ?: run {
        try {
            val icon = apiService.getIcons().find { it.id == id }
            if (icon != null) {
                iconDao.insertAll(listOf(icon))
            }
            icon
        } catch (e: Exception) {
            null
        }
    }
}