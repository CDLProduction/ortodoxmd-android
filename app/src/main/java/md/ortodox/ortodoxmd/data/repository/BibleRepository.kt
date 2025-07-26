package md.ortodox.ortodoxmd.data.repository

import md.ortodox.ortodoxmd.data.dao.BibleDao
import md.ortodox.ortodoxmd.data.network.BibleApiService

import md.ortodox.ortodoxmd.data.model.bible.BibleBook
import md.ortodox.ortodoxmd.data.model.bible.BibleChapter
import md.ortodox.ortodoxmd.data.model.bible.BibleVerse
import md.ortodox.ortodoxmd.data.model.bible.BibleBookmark
import md.ortodox.ortodoxmd.data.model.bible.VerseWithBookInfo
import javax.inject.Inject

class BibleRepository @Inject constructor(
    private val apiService: BibleApiService,
    private val bibleDao: BibleDao
) {
    suspend fun getBooks(testamentId: Long?): List<BibleBook> {
        val cached = bibleDao.getBooksByTestament(testamentId)
        if (cached.isNotEmpty()) return cached
        val data = apiService.getBooks(testamentId)
        bibleDao.insertBooks(data)
        return data
    }

    suspend fun getChapters(bookId: Long): List<BibleChapter> {
        val cached = bibleDao.getChaptersByBook(bookId)
        if (cached.isNotEmpty()) return cached
        val data = apiService.getChapters(bookId)
        bibleDao.insertChapters(data)
        return data
    }

    suspend fun getVerses(bookId: Long, chapterNumber: Int): List<BibleVerse> {
        val cached = bibleDao.getVersesByChapter(bookId, chapterNumber)
        if (cached.isNotEmpty()) return cached
        val data = apiService.getVerses(bookId, chapterNumber)
        bibleDao.insertVerses(data)
        return data
    }

    // Folosește noua metodă și noul model de date
    suspend fun searchVersesWithBookInfo(query: String): List<VerseWithBookInfo> {
        return bibleDao.searchVersesWithBookInfo(query)
    }

    suspend fun getBookmark(): BibleBookmark? = bibleDao.getBookmark()

    suspend fun saveBookmark(bookmark: BibleBookmark) = bibleDao.saveBookmark(bookmark)

}