package md.ortodox.ortodoxmd.ui.audiobook

import androidx.work.WorkInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity

data class AudiobooksUiState(
    val categories: ImmutableList<AudiobookCategory> = persistentListOf(),
    val isLoading: Boolean = true,
    val downloadStates: ImmutableMap<Long, WorkInfo.State> = persistentMapOf(),
    val downloadProgress: ImmutableMap<Long, Int> = persistentMapOf()
)

data class ChapterScreenState(
    val book: AudiobookBook? = null,
    val isLoading: Boolean = true
)

data class DownloadedAudiobooksUiState(
    val categories: ImmutableList<AudiobookCategory> = persistentListOf(),
    val isLoading: Boolean = true
)

data class AudiobookCategory(
    val name: String,
    val books: ImmutableList<AudiobookBook>,
    val isSimpleCategory: Boolean = false
)

data class AudiobookBook(
    val name: String,
    val testament: String,
    val chapters: ImmutableList<AudiobookEntity>
)