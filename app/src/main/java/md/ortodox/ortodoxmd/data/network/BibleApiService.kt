package md.ortodox.ortodoxmd.data.network

import com.google.gson.annotations.SerializedName
import md.ortodox.ortodoxmd.data.model.bible.BibleBook
import md.ortodox.ortodoxmd.data.model.bible.BibleChapter
import md.ortodox.ortodoxmd.data.model.bible.BibleVerse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// --- DTO-uri pentru endpoint-ul /books ---
data class BookResponseDto(
    val id: Long,
    val nameRo: String,
    val testament: TestamentNestedDto
)

// --- DTO-uri pentru endpoint-ul /verses ---
data class VerseResponseDto(
    val id: Long,
    val verseNumber: Int,
    val textRo: String,
    val chapter: ChapterNestedDto
)

data class ChapterNestedDto(
    val id: Long,
    val chapterNumber: Int,
    val book: BookNestedDto
)

data class BookNestedDto(
    val id: Long,
    val nameRo: String,
    val testament: TestamentNestedDto
)

// --- DTO-uri pentru endpoint-ul /all ---
data class AllBooksResponseDto(
    val id: Long,
    val nameRo: String,
    @SerializedName("testamentId") val testamentId: Long,
    val chapters: List<AllChaptersResponseDto>
)

data class AllChaptersResponseDto(
    @SerializedName("chapterNumber") val chapterNumber: Int,
    val verses: List<AllVersesResponseDto>
)

data class AllVersesResponseDto(
    val id: Long,
    val verseNumber: Int,
    val textRo: String,
    // Aceste câmpuri sunt esențiale pentru a popula corect baza de date
    @SerializedName("bookId") val bookId: Long,
    @SerializedName("chapterNumber") val chapterNumber: Int
)

// --- DTO comun pentru testament ---
data class TestamentNestedDto(
    val id: Long,
    val nameRo: String
)


interface BibleApiService {
    // MODIFICAT: Folosește noul DTO pentru a parsa răspunsul
    @GET("api/bible/books")
    suspend fun getBooks(@Query("testamentId") testamentId: Long?): List<BookResponseDto>

    // Acest endpoint nu pare să fie folosit direct, dar îl lăsăm pentru consistență
    @GET("api/bible/books/{bookId}/chapters")
    suspend fun getChapters(@Path("bookId") bookId: Long): List<BibleChapter>

    // MODIFICAT: Folosește noul DTO pentru a parsa răspunsul
    @GET("api/bible/books/{bookId}/chapters/{chapterNumber}/verses")
    suspend fun getVerses(@Path("bookId") bookId: Long, @Path("chapterNumber") chapterNumber: Int): List<VerseResponseDto>

    // MODIFICAT: Folosește noul DTO pentru a parsa răspunsul de la /all
    @GET("api/bible/all")
    suspend fun getEntireBible(): List<AllBooksResponseDto>
}
