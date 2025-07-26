package md.ortodox.ortodoxmd.data.repository

import md.ortodox.ortodoxmd.data.dao.BibleDao
import md.ortodox.ortodoxmd.data.model.bible.*
import md.ortodox.ortodoxmd.data.network.BibleApiService
import md.ortodox.ortodoxmd.ui.bible.ParsedReference
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

    suspend fun searchVersesWithBookInfo(query: String): List<VerseWithBookInfo> {
        return bibleDao.searchVersesWithBookInfo(query)
    }

    // NOU: Metodă pentru a căuta după referință.
    suspend fun getVersesByReference(reference: ParsedReference): List<BibleVerse> {
        return bibleDao.getVersesByReference(
            bookName = reference.bookName,
            chapter = reference.chapter,
            startVerse = reference.startVerse,
            endVerse = reference.endVerse
        )
    }

    // NOU: Metodă pentru a prelua semnul de carte cu detalii.
    suspend fun getBookmarkWithDetails(): BookmarkWithDetails? = bibleDao.getBookmarkWithDetails()

    suspend fun saveBookmark(bookmark: BibleBookmark) = bibleDao.saveBookmark(bookmark)
}