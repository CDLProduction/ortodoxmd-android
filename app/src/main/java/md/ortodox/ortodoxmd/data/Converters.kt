package md.ortodox.ortodoxmd.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import md.ortodox.ortodoxmd.data.model.Saint

class Converters {
    @TypeConverter
    fun fromSaintsList(saints: List<Saint>): String {
        return Gson().toJson(saints)
    }

    @TypeConverter
    fun toSaintsList(saintsJson: String): List<Saint> {
        val type = object : TypeToken<List<Saint>>() {}.type
        return Gson().fromJson(saintsJson, type) ?: emptyList()
    }
}