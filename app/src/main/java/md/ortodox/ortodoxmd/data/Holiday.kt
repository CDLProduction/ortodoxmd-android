package md.ortodox.ortodoxmd.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "holidays")
data class Holiday(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,  // ex: "2025-07-23"
    val name: String,  // ex: "Sf. Pantelimon"
    val type: String   // ex: "Sărbătoare mare"
)