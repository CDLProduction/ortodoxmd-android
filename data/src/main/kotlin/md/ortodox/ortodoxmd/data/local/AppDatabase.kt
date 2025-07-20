package md.ortodox.ortodoxmd.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import md.ortodox.ortodoxmd.data.model.Saint  // Importă din model
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


@Database(entities = [CalendarEntity::class], version = 1, exportSchema = true)
@TypeConverters(SaintsConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun calendarDao(): CalendarDao
}

@Entity(tableName = "calendar")
data class CalendarEntity(
    @PrimaryKey val date: String,  // PK bazat pe dată
    val fastingType: String,
    val fastingDescriptionEn: String,
    val fastingDescriptionRo: String,
    val fastingDescriptionRu: String,
    val summaryTitleEn: String,
    val summaryTitleRo: String,
    val summaryTitleRu: String,
    val titlesEn: String,
    val titlesRo: String,
    val titlesRu: String,
    val saints: List<Saint>,
    val fastingDay: Boolean
)

class SaintsConverter {
    @TypeConverter
    fun fromSaintsList(saints: List<Saint>): String = Gson().toJson(saints)

    @TypeConverter
    fun toSaintsList(json: String): List<Saint> {
        val type = object : TypeToken<List<Saint>>() {}.type
        return Gson().fromJson(json, type)
    }
}

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar WHERE date = :date")
    suspend fun getByDate(date: String): CalendarEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(calendar: CalendarEntity)
}