package md.ortodox.ortodoxmd.data.network

import md.ortodox.ortodoxmd.data.model.bible.BibleBook
import md.ortodox.ortodoxmd.data.model.bible.BibleChapter
import md.ortodox.ortodoxmd.data.model.bible.BibleVerse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BibleApiService {
    @GET("api/bible/books")
    suspend fun getBooks(@Query("testamentId") testamentId: Long?): List<BibleBook>

    @GET("api/bible/books/{bookId}/chapters")
    suspend fun getChapters(@Path("bookId") bookId: Long): List<BibleChapter>

    @GET("api/bible/books/{bookId}/chapters/{chapterNumber}/verses")
    suspend fun getVerses(@Path("bookId") bookId: Long, @Path("chapterNumber") chapterNumber: Int): List<BibleVerse>
}