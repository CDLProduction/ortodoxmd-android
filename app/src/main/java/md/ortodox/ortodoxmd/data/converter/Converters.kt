package md.ortodox.ortodoxmd.data.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import md.ortodox.ortodoxmd.data.model.Saint
import md.ortodox.ortodoxmd.data.model.Prayer
import md.ortodox.ortodoxmd.data.model.bible.BibleBook
import md.ortodox.ortodoxmd.data.model.bible.BibleChapter
import md.ortodox.ortodoxmd.data.model.bible.BibleTestament
import md.ortodox.ortodoxmd.data.model.bible.BibleVerse

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

    @TypeConverter
    fun fromPrayerList(prayers: List<Prayer>): String {
        return Gson().toJson(prayers)
    }

    @TypeConverter
    fun toPrayerList(json: String): List<Prayer> {
        val type = object : TypeToken<List<Prayer>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }

    // Adăugat pentru Biblie: Converter pentru List<BibleBook>
    @TypeConverter
    fun fromBibleBookList(books: List<BibleBook>): String {
        return Gson().toJson(books)
    }

    @TypeConverter
    fun toBibleBookList(json: String): List<BibleBook> {
        val type = object : TypeToken<List<BibleBook>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }

    // Adăugat pentru Biblie: Converter pentru List<BibleChapter>
    @TypeConverter
    fun fromBibleChapterList(chapters: List<BibleChapter>): String {
        return Gson().toJson(chapters)
    }

    @TypeConverter
    fun toBibleChapterList(json: String): List<BibleChapter> {
        val type = object : TypeToken<List<BibleChapter>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }

    // Adăugat pentru Biblie: Converter pentru List<BibleVerse>
    @TypeConverter
    fun fromBibleVerseList(verses: List<BibleVerse>): String {
        return Gson().toJson(verses)
    }

    @TypeConverter
    fun toBibleVerseList(json: String): List<BibleVerse> {
        val type = object : TypeToken<List<BibleVerse>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }
    @TypeConverter
    fun fromBibleTestament(testament: BibleTestament): String {
        return Gson().toJson(testament)
    }

    @TypeConverter
    fun toBibleTestament(json: String): BibleTestament {
        val type = object : TypeToken<BibleTestament>() {}.type
        return Gson().fromJson(json, type)
    }
}