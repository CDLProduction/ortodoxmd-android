package md.ortodox.ortodoxmd.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
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

    // NOU: Interogare pentru căutare după referință (ex: "Matei 2:15").
    @Query("""
        SELECT v.* FROM bible_verses v
        JOIN bible_books b ON v.bookId = b.id
        WHERE b.nameRo LIKE '%' || :bookName || '%' AND v.chapterNumber = :chapter
        AND v.verseNumber >= :startVerse AND (:endVerse IS NULL OR v.verseNumber <= :endVerse)
    """)
    suspend fun getVersesByReference(bookName: String, chapter: Int, startVerse: Int, endVerse: Int?): List<BibleVerse>

    // NOU: Interogare pentru a prelua semnul de carte cu numele cărții.
    @Query("""
        SELECT bm.*, b.nameRo FROM bible_bookmarks bm
        JOIN bible_books b ON bm.bookId = b.id
        LIMIT 1
    """)
    suspend fun getBookmarkWithDetails(): BookmarkWithDetails?

    @Upsert
    suspend fun saveBookmark(bookmark: BibleBookmark)
}