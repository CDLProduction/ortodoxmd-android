package md.ortodox.ortodoxmd.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import md.ortodox.ortodoxmd.data.model.bible.*

@Dao
interface BibleDao {
    @Upsert
    suspend fun insertBooks(books: List<BibleBook>)

    @Query("SELECT * FROM bible_books WHERE testamentId = :testamentId OR :testamentId IS NULL")
    suspend fun getBooksByTestament(testamentId: Long?): List<BibleBook>

    @Upsert
    suspend fun insertChapters(chapters: List<BibleChapter>)

    @Query("SELECT * FROM bible_chapters WHERE bookId = :bookId")
    suspend fun getChaptersByBook(bookId: Long): List<BibleChapter>

    @Upsert
    suspend fun insertVerses(verses: List<BibleVerse>)

    @Query("SELECT * FROM bible_verses WHERE bookId = :bookId AND chapterNumber = :chapterNumber")
    suspend fun getVersesByChapter(bookId: Long, chapterNumber: Int): List<BibleVerse>

    @Query("""
        SELECT v.*, b.nameRo as bookNameRo
        FROM bible_verses v
        JOIN bible_books b ON v.bookId = b.id
        WHERE v.textRo LIKE '%' || :query || '%'
    """)
    suspend fun searchVersesWithBookInfo(query: String): List<VerseWithBookInfo>

    @Query("""
        SELECT v.* FROM bible_verses v
        JOIN bible_books b ON v.bookId = b.id
        WHERE LOWER(b.nameRo) LIKE '%' || LOWER(:bookName) || '%' AND v.chapterNumber = :chapter
        AND v.verseNumber >= :startVerse AND (:endVerse IS NULL OR v.verseNumber <= :endVerse)
    """)
    suspend fun getVersesByReference(bookName: String, chapter: Int, startVerse: Int, endVerse: Int?): List<BibleVerse>

    @Upsert
    suspend fun addBookmark(bookmark: BibleBookmark)

    @Query("DELETE FROM bible_bookmarks WHERE verseId = :verseId")
    suspend fun removeBookmark(verseId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM bible_bookmarks WHERE verseId = :verseId LIMIT 1)")
    suspend fun isBookmarked(verseId: Long): Boolean

    @Query("SELECT verseId FROM bible_bookmarks")
    fun getBookmarkedVerseIds(): Flow<List<Long>>

    @Query("""
        SELECT v.*, b.nameRo as bookNameRo
        FROM bible_verses v
        JOIN bible_bookmarks bm ON v.id = bm.verseId
        JOIN bible_books b ON v.bookId = b.id
        ORDER BY v.bookId, v.chapterNumber, v.verseNumber
    """)
    fun getAllBookmarksWithDetails(): Flow<List<VerseWithBookInfo>>

    // --- NOU: Metode pentru descărcarea completă a Bibliei ---

    @Query("DELETE FROM bible_verses")
    suspend fun clearAllVerses()

    @Query("DELETE FROM bible_chapters")
    suspend fun clearAllChapters()

    @Query("DELETE FROM bible_books")
    suspend fun clearAllBooks()

    /**
     * Înlocuiește complet datele Bibliei într-o singură tranzacție.
     * Acest lucru asigură că nu există stări intermediare inconsistente.
     */
    @Transaction
    suspend fun replaceEntireBible(books: List<BibleBook>, chapters: List<BibleChapter>, verses: List<BibleVerse>) {
        // Șterge datele vechi
        clearAllVerses()
        clearAllChapters()
        clearAllBooks()
        // Inserează datele noi
        insertBooks(books)
        insertChapters(chapters)
        insertVerses(verses)
    }

    // NOU: Metodă pentru a verifica dacă Biblia este descărcată
    @Query("SELECT COUNT(*) FROM bible_books")
    suspend fun countBooks(): Int
}
