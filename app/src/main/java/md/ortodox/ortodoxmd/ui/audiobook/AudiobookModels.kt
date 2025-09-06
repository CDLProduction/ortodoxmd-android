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
    val downloadProgress: ImmutableMap<Long, Int> = persistentMapOf(),
    val syncError: String? = null
)

data class ChapterScreenState(
    val book: AudiobookBook? = null,
    val isLoading: Boolean = true,
    val paginatedChapters: PaginatedChapters? = null,
    val lazyLoadingState: LazyLoadingState = LazyLoadingState()
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

data class DownloadStateInfo(
    val state: WorkInfo.State?,
    val progress: Int
)

data class PaginatedChapters(
    val chapters: ImmutableList<AudiobookEntity>,
    val hasMorePages: Boolean,
    val currentPage: Int,
    val totalChapters: Int
) {
    companion object {
        const val PAGE_SIZE = 20 // Load 20 chapters at a time
    }
}

data class LazyLoadingState(
    val isLoadingMore: Boolean = false,
    val hasReachedEnd: Boolean = false,
    val error: String? = null
)