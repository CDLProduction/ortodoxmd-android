package md.ortodox.ortodoxmd.data.model.bible

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * REFACTORIZAT: Entitate pentru a stoca semnele de carte.
 * Acum, fiecare verset marcat este o intrare separată în tabel.
 * Cheia primară este ID-ul versetului pentru a asigura unicitatea.
 */
@Entity(tableName = "bible_bookmarks")
data class BibleBookmark(
    @PrimaryKey
    val verseId: Long
)
