package md.ortodox.ortodoxmd.data.repository

import android.util.Log
import md.ortodox.ortodoxmd.data.dao.BibleDao
import md.ortodox.ortodoxmd.data.model.bible.*
import md.ortodox.ortodoxmd.data.network.BibleApiService
import md.ortodox.ortodoxmd.ui.bible.ParsedReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class BibleRepository @Inject constructor(
    private val apiService: BibleApiService,
    private val bibleDao: BibleDao
) {

    /**
     * Preia cărțile (dacă e nevoie, de la API) și le transformă în formatul pentru DB.
     */
    suspend fun getBooks(testamentId: Long?): Result<List<BibleBook>> {
        return try {
            val cached = bibleDao.getBooksByTestament(testamentId)
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                val fromApi = apiService.getBooks(testamentId)
                // Maparea de la DTO la entitatea de DB
                val booksToStore = fromApi.map { bookDto ->
                    BibleBook(
                        id = bookDto.id,
                        nameRo = bookDto.nameRo,
                        nameEn = "", // Poți adăuga ulterior
                        nameRu = "", // Poți adăuga ulterior
                        testamentId = bookDto.testament.id
                    )
                }
                bibleDao.insertBooks(booksToStore)
                Result.success(booksToStore)
            }
        } catch (e: Exception) {
            Log.e("BibleRepository", "Failed to get books", e)
            Result.failure(e)
        }
    }

    /**
     * Preia versetele (dacă e nevoie, de la API) și le transformă în formatul pentru DB.
     */
    suspend fun getVerses(bookId: Long, chapterNumber: Int): List<BibleVerse> {
        val cached = bibleDao.getVersesByChapter(bookId, chapterNumber)
        if (cached.isNotEmpty()) return cached

        val fromApi = apiService.getVerses(bookId, chapterNumber)
        // Maparea de la DTO la entitatea de DB
        val versesToStore = fromApi.map { verseDto ->
            BibleVerse(
                id = verseDto.id,
                bookId = verseDto.chapter.book.id,
                chapterNumber = verseDto.chapter.chapterNumber,
                verseNumber = verseDto.verseNumber,
                textRo = verseDto.textRo,
                textEn = null,
                textRu = null
            )
        }
        bibleDao.insertVerses(versesToStore)
        return versesToStore
    }


    suspend fun isBibleDownloaded(): Boolean {
        // Verificăm dacă există un număr rezonabil de cărți în DB.
        // Ajustează '70' dacă numărul total de cărți este diferit.
        return bibleDao.countBooks() > 70
    }

    /**
     * Descarcă întreaga Biblie de la /all și o salvează în baza de date.
     */
    fun downloadAndCacheEntireBible(): Flow<Float> = flow {
        try {
            emit(0.0f) // Start
            val allBooksFromApi = apiService.getEntireBible()
            val totalBooks = allBooksFromApi.size.toFloat()

            if (totalBooks == 0f) {
                throw Exception("API returned an empty list of books.")
            }

            val booksToStore = mutableListOf<BibleBook>()
            val chaptersToStore = mutableListOf<BibleChapter>()
            val versesToStore = mutableListOf<BibleVerse>()

            allBooksFromApi.forEachIndexed { index, bookDto ->
                // Extrage cartea
                booksToStore.add(
                    BibleBook(
                        id = bookDto.id,
                        nameRo = bookDto.nameRo,
                        nameEn = "",
                        nameRu = "",
                        testamentId = bookDto.testamentId
                    )
                )

                // Extrage capitolele și versetele din cartea curentă
                bookDto.chapters.forEach { chapterDto ->
                    chaptersToStore.add(
                        BibleChapter(
                            bookId = bookDto.id, // ID-ul cărții părinte
                            chapterNumber = chapterDto.chapterNumber
                        )
                    )
                    chapterDto.verses.forEach { verseDto ->
                        versesToStore.add(
                            BibleVerse(
                                id = verseDto.id,
                                bookId = verseDto.bookId, // Folosim ID-urile direct din verset
                                chapterNumber = verseDto.chapterNumber, // Folosim ID-urile direct din verset
                                verseNumber = verseDto.verseNumber,
                                textRo = verseDto.textRo,
                                textEn = null,
                                textRu = null
                            )
                        )
                    }
                }
                // Emite progresul
                emit((index + 1) / totalBooks)
            }

            // Înlocuiește datele într-o singură tranzacție
            bibleDao.replaceEntireBible(
                books = booksToStore,
                chapters = chaptersToStore,
                verses = versesToStore
            )

        } catch (e: Exception) {
            Log.e("BibleRepository", "Error downloading entire Bible", e)
            throw e // Aruncă excepția pentru a fi prinsă în ViewModel
        }
    }

    // --- Restul metodelor rămân neschimbate ---
    suspend fun getChapters(bookId: Long): List<BibleChapter> = bibleDao.getChaptersByBook(bookId)
    suspend fun searchVersesWithBookInfo(query: String): List<VerseWithBookInfo> = bibleDao.searchVersesWithBookInfo(query)
    suspend fun getVersesByReference(reference: ParsedReference): List<BibleVerse> = bibleDao.getVersesByReference(reference.bookName, reference.chapter, reference.startVerse, reference.endVerse)
    suspend fun toggleBookmark(verseId: Long) { if (bibleDao.isBookmarked(verseId)) bibleDao.removeBookmark(verseId) else bibleDao.addBookmark(BibleBookmark(verseId = verseId)) }
    fun getBookmarkedVerseIds(): Flow<List<Long>> = bibleDao.getBookmarkedVerseIds()
    fun getAllBookmarksWithDetails(): Flow<List<VerseWithBookInfo>> = bibleDao.getAllBookmarksWithDetails()
}
