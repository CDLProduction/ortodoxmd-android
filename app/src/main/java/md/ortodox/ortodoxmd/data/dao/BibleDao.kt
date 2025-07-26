package md.ortodox.ortodoxmd.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import md.ortodox.ortodoxmd.data.model.bible.BibleBook
import md.ortodox.ortodoxmd.data.model.bible.BibleChapter
import md.ortodox.ortodoxmd.data.model.bible.BibleVerse
import md.ortodox.ortodoxmd.data.model.bible.BibleBookmark
import md.ortodox.ortodoxmd.data.model.bible.VerseWithBookInfo

@Dao
interface BibleDao {
    @Upsert
    suspend fun insertBooks(books: List<BibleBook>)

    @Query("SELECT * FROM bible_books WHERE testamentId = :testamentId")
    suspend fun getBooksByTestament(testamentId: Long?): List<BibleBook>

    @Upsert
    suspend fun insertChapters(chapters: List<BibleChapter>)

    @Query("SELECT * FROM bible_chapters WHERE bookId = :bookId")
    suspend fun getChaptersByBook(bookId: Long): List<BibleChapter>

    @Upsert
    suspend fun insertVerses(verses: List<BibleVerse>)

    @Query("SELECT * FROM bible_verses WHERE bookId = :bookId AND chapterNumber = :chapterNumber")
    suspend fun getVersesByChapter(bookId: Long, chapterNumber: Int): List<BibleVerse>

    //    @Query("SELECT * FROM bible_verses WHERE textRo LIKE :query")
//    suspend fun searchVerses(query: String): List<BibleVerse>
    @Query(
        """
        SELECT v.*, b.nameRo as bookNameRo
        FROM bible_verses v
        JOIN bible_books b ON v.bookId = b.id
        WHERE v.textRo LIKE '%' || :query || '%'
    """
    )
    suspend fun searchVersesWithBookInfo(query: String): List<VerseWithBookInfo>

    @Query("SELECT * FROM bible_bookmarks LIMIT 1")
    suspend fun getBookmark(): BibleBookmark?

    @Upsert
    suspend fun saveBookmark(bookmark: BibleBookmark)


}