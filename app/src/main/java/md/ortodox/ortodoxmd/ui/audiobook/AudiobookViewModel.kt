package md.ortodox.ortodoxmd.ui.audiobook

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import md.ortodox.ortodoxmd.data.model.audiobook.AudiobookEntity
import md.ortodox.ortodoxmd.data.repository.AudiobookRepository
import md.ortodox.ortodoxmd.data.worker.AudioDownloadWorker
import javax.inject.Inject


@HiltViewModel
class AudiobookViewModel @Inject constructor(
    private val repository: AudiobookRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val workManager = WorkManager.getInstance(context)

    // --- FLUXURI DE BAZĂ (PRIVATE) ---
    
    // Cache for transformed categories to avoid recomputation
    private var categoriesCache: ImmutableList<AudiobookCategory>? = null
    private var cacheKey: Int = -1

    private val audiobooksStructure: StateFlow<ImmutableList<AudiobookCategory>> = repository.getAudiobooks()
        .map { audiobooks -> 
            val newCacheKey = audiobooks.hashCode()
            if (categoriesCache != null && cacheKey == newCacheKey) {
                categoriesCache!!
            } else {
                val result = transformToGroupedCategoriesOptimized(audiobooks)
                categoriesCache = result
                cacheKey = newCacheKey
                result
            }
        }
        .flowOn(Dispatchers.Default)
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(15000), persistentListOf())

    // Optimized WorkManager monitoring with intelligent batching
    private val downloadInfo: StateFlow<Pair<ImmutableMap<Long, WorkInfo.State>, ImmutableMap<Long, Int>>> = repository.getDownloadWorkInfoFlow()
        .map { workInfos ->
            // Pre-allocate maps with expected capacity for better performance
            val statesMap = mutableMapOf<Long, WorkInfo.State>()
            val progressMap = mutableMapOf<Long, Int>()
            
            // Process WorkManager updates
            
            // Single pass through work infos with early filtering
            workInfos.forEach { workInfo ->
                val audiobookId = extractAudiobookIdFromTags(workInfo.tags)
                
                if (audiobookId != -1L) {
                    statesMap[audiobookId] = workInfo.state
                    // Only track progress for running downloads to reduce memory usage
                    if (workInfo.state == WorkInfo.State.RUNNING) {
                        progressMap[audiobookId] = workInfo.progress.getInt(AudioDownloadWorker.KEY_PROGRESS, 0)
                    }
                }
            }
            
            statesMap.toImmutableMap() to progressMap.toImmutableMap()
        }
        .flowOn(Dispatchers.Default)
        .distinctUntilChanged { old, new ->
            // Custom equality check to reduce unnecessary updates
            old.first.size == new.first.size && 
            old.first.keys == new.first.keys &&
            old.first.values.zip(new.first.values).all { (oldState, newState) -> oldState == newState }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(15000), Pair(persistentMapOf(), persistentMapOf()))

    // --- STĂRI PUBLICE PENTRU UI ---
    
    private val _syncError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AudiobooksUiState> = combine(
        audiobooksStructure,
        downloadInfo,
        _syncError
    ) { structure, (states, progress), syncError ->
        AudiobooksUiState(
            categories = structure,
            isLoading = structure.isEmpty() && syncError == null,
            downloadStates = states,
            downloadProgress = progress,
            syncError = syncError
        )
    }
        .flowOn(Dispatchers.Default)
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(10000), AudiobooksUiState(isLoading = true))

    // Cache for downloaded audiobooks to avoid recomputation
    private var downloadedCache: ImmutableList<AudiobookCategory>? = null
    private var downloadedCacheKey: Int = -1

    val downloadedUiState: StateFlow<DownloadedAudiobooksUiState> = repository.getDownloadedAudiobooks()
        .map { downloadedAudiobooks -> 
            val newCacheKey = downloadedAudiobooks.hashCode()
            val categories = if (downloadedCache != null && downloadedCacheKey == newCacheKey) {
                downloadedCache!!
            } else {
                val result = transformToGroupedCategoriesOptimized(downloadedAudiobooks)
                downloadedCache = result
                downloadedCacheKey = newCacheKey
                result
            }
            
            DownloadedAudiobooksUiState(
                categories = categories,
                isLoading = false
            )
        }
        .flowOn(Dispatchers.Default)
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(10000),
            initialValue = DownloadedAudiobooksUiState(isLoading = true)
        )

    val isDownloading: StateFlow<Boolean> = downloadInfo
        .map { (states, _) -> 
            states.values.any { it == WorkInfo.State.RUNNING || it == WorkInfo.State.ENQUEUED } 
        }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(10000), false)

    private val _selectedBookName = MutableStateFlow<String?>(null)
    private val _currentChapterPage = MutableStateFlow(0)
    private val _lazyLoadingState = MutableStateFlow(LazyLoadingState())
    
    val selectedBookState: StateFlow<ChapterScreenState> = combine(
        _selectedBookName.filterNotNull(),
        audiobooksStructure,
        _currentChapterPage,
        _lazyLoadingState
    ) { selectedName, categories, currentPage, lazyState ->
        // Use pre-computed display name directly for better performance
        val displayName = selectedName.toDisplayableName()
        val book = categories.asSequence()
            .flatMap { it.books.asSequence() }
            .find { it.name == displayName }
            
        if (book != null) {
            // Implement pagination for large chapter lists
            val totalChapters = book.chapters.size
            val pageSize = PaginatedChapters.PAGE_SIZE
            val startIndex = 0
            val endIndex = minOf((currentPage + 1) * pageSize, totalChapters)
            
            val visibleChapters = if (totalChapters > pageSize) {
                book.chapters.subList(startIndex, endIndex).toImmutableList()
            } else {
                book.chapters
            }
            
            val paginatedChapters = PaginatedChapters(
                chapters = visibleChapters,
                hasMorePages = endIndex < totalChapters,
                currentPage = currentPage,
                totalChapters = totalChapters
            )
            
            ChapterScreenState(
                book = book,
                isLoading = false,
                paginatedChapters = paginatedChapters,
                lazyLoadingState = lazyState
            )
        } else {
            ChapterScreenState(isLoading = book == null)
        }
    }
        .flowOn(Dispatchers.Default)
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(10000), initialValue = ChapterScreenState(isLoading = true))

    // --- ACȚIUNI ---

    init {
        viewModelScope.launch { 
            try {
                repository.syncAudiobooks() 
                // Prefetch download states after initial sync
                prefetchDownloadStates()
                _syncError.value = null
            } catch (e: Exception) {
                // Log the error but don't crash - app should work with cached data when servers are down
                Log.e("AudiobookViewModel", "Failed to sync audiobooks from server: ${e.message}", e)
                _syncError.value = "Serverul nu este disponibil. Se afișează conținutul salvat local."
            }
        }
    }
    
    // Prefetch download states for smooth scrolling
    private fun prefetchDownloadStates() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                // Trigger initial flow collection to warm up caches
                downloadInfo.value
                
                // Pre-warm the most commonly accessed data
                audiobooksStructure.value.take(3).forEach { category ->
                    category.books.take(5).forEach { book ->
                        // Pre-compute chapter state information
                        book.chapters.forEach { chapter ->
                            downloadInfo.value.first[chapter.id]
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("AudiobookViewModel", "Prefetch completed with minor issues: ${e.message}")
            }
        }
    }

    fun refreshAudiobooks() {
        viewModelScope.launch {
            try {
                repository.syncAudiobooks()
                _syncError.value = null
                Log.d("AudiobookViewModel", "Successfully synced audiobooks")
            } catch (e: Exception) {
                Log.e("AudiobookViewModel", "Failed to sync audiobooks: ${e.message}", e)
                _syncError.value = "Serverul nu este disponibil. Se afișează conținutul salvat local."
            }
        }
    }
    
    fun selectBook(bookName: String) { 
        _selectedBookName.value = bookName 
        _currentChapterPage.value = 0 // Reset pagination
        _lazyLoadingState.value = LazyLoadingState()
    }
    
    fun clearBookSelection() { 
        _selectedBookName.value = null 
        _currentChapterPage.value = 0
        _lazyLoadingState.value = LazyLoadingState()
    }
    
    // Load more chapters for pagination
    fun loadMoreChapters() {
        val currentState = selectedBookState.value
        if (currentState.lazyLoadingState.isLoadingMore || 
            currentState.lazyLoadingState.hasReachedEnd || 
            currentState.paginatedChapters?.hasMorePages != true) {
            return
        }
        
        viewModelScope.launch {
            _lazyLoadingState.value = LazyLoadingState(isLoadingMore = true)
            
            try {
                // Simulate loading delay for smooth UX
                kotlinx.coroutines.delay(200)
                
                // Load next page
                _currentChapterPage.value = _currentChapterPage.value + 1
                
                // Check if we've reached the end
                val newState = selectedBookState.value
                val hasReachedEnd = newState.paginatedChapters?.hasMorePages == false
                
                _lazyLoadingState.value = LazyLoadingState(
                    isLoadingMore = false,
                    hasReachedEnd = hasReachedEnd
                )
                
            } catch (e: Exception) {
                _lazyLoadingState.value = LazyLoadingState(
                    isLoadingMore = false,
                    error = "Failed to load more chapters: ${e.message}"
                )
                Log.e("AudiobookViewModel", "Error loading more chapters", e)
            }
        }
    }

    fun downloadChapter(chapter: AudiobookEntity) = viewModelScope.launch { 
        repository.startDownload(chapter) 
    }
    fun deleteChapter(chapter: AudiobookEntity) = viewModelScope.launch { repository.deleteChapter(chapter) }
    fun cancelAllDownloads() = viewModelScope.launch { 
        workManager.cancelAllWorkByTag(AudiobookRepository.DOWNLOAD_TAG)
    }
    fun deleteAllDownloadedChapters(chapters: List<AudiobookEntity>) = viewModelScope.launch {
        repository.deleteChapters(chapters.filter { it.isDownloaded })
    }

    fun downloadAllChapters(chapters: List<AudiobookEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentDownloadStates = downloadInfo.value.first
            
            // Filter chapters that need downloading with optimized logic
            val chaptersToDownload = chapters.filter { chapter ->
                val state = currentDownloadStates[chapter.id]
                !chapter.isDownloaded && 
                state != WorkInfo.State.SUCCEEDED && 
                state != WorkInfo.State.ENQUEUED && 
                state != WorkInfo.State.RUNNING
            }
            
            if (chaptersToDownload.isEmpty()) {
                return@launch
            }

            // Process downloads in smaller batches for better performance
            val batchSize = 3 // Limit concurrent downloads
            chaptersToDownload.chunked(batchSize).forEach { batch ->
                val workRequests = batch.map { chapter ->
                    repository.createDownloadWorkRequest(chapter)
                }

                // Create sequential chain for this batch
                if (workRequests.isNotEmpty()) {
                    var continuation = workManager.beginUniqueWork(
                        "download_batch_${System.currentTimeMillis()}",
                        ExistingWorkPolicy.APPEND_OR_REPLACE,
                        workRequests.first()
                    )

                    // Chain remaining requests in this batch
                    for (i in 1 until workRequests.size) {
                        continuation = continuation.then(workRequests[i])
                    }

                    continuation.enqueue()
                }
            }
            
        }
    }


    // --- FUNCȚII PRIVATE AJUTĂTOARE ---

    private fun extractAudiobookIdFromTags(tags: Set<String>): Long {
        return tags.find { it.startsWith("audiobook_download_") }?.substringAfter("audiobook_download_")?.toLongOrNull() ?: -1L
    }

    // Memory-optimized transformation using object pools and efficient collections
    private fun transformToGroupedCategoriesOptimized(audiobooks: List<AudiobookEntity>): ImmutableList<AudiobookCategory> {
        if (audiobooks.isEmpty()) return persistentListOf()
        
        // Use memory pool for temporary collections
        val tempCategories = AudiobookMemoryPool.getMutableList<AudiobookCategory>()
        
        try {
            // Use pre-computed category names for grouping with LinkedHashMap for better performance
            val groupedByCategory = LinkedHashMap<String, MutableList<AudiobookEntity>>()
            
            // Single pass grouping with reusable lists
            audiobooks.forEach { audiobook ->
                val categoryList = groupedByCategory.getOrPut(audiobook.categoryName) {
                    AudiobookMemoryPool.getMutableList()
                }
                categoryList.add(audiobook)
            }
            
            // Process each category
            groupedByCategory.forEach { (categoryName, chaptersInCategory) ->
                // Use memory pool for temporary book collections
                val tempBooks = AudiobookMemoryPool.getMutableList<AudiobookBook>()
                
                try {
                    // Group by book names efficiently
                    val bookGroups = LinkedHashMap<String, MutableList<AudiobookEntity>>()
                    chaptersInCategory.forEach { chapter ->
                        val bookList = bookGroups.getOrPut(chapter.bookName) {
                            AudiobookMemoryPool.getMutableList()
                        }
                        bookList.add(chapter)
                    }
                    
                    // Create books with pre-sorted chapters
                    bookGroups.forEach { (bookName, chaptersInBook) ->
                        val testamentName = chaptersInBook.first().testamentName
                        val sortedChapters = chaptersInBook.sortedBy { it.chapterNumber }.toImmutableList()
                        
                        tempBooks.add(
                            AudiobookBook(
                                name = bookName,
                                testament = testamentName,
                                chapters = sortedChapters
                            )
                        )
                        
                        // Return book chapter list to pool
                        AudiobookMemoryPool.recycleMutableList(chaptersInBook)
                    }
                    
                    // Sort books and create category
                    tempBooks.sortBy { it.name }
                    val category = AudiobookCategory(
                        name = categoryName,
                        books = tempBooks.toImmutableList(),
                        isSimpleCategory = tempBooks.size == 1 && tempBooks.first().name.equals(categoryName, ignoreCase = true)
                    )
                    tempCategories.add(category)
                    
                } finally {
                    // Return temporary book list to pool
                    AudiobookMemoryPool.recycleMutableList(tempBooks)
                }
                
                // Return category chapter list to pool
                AudiobookMemoryPool.recycleMutableList(chaptersInCategory)
            }
            
            return tempCategories.toImmutableList()
            
        } finally {
            // Always return the main category list to pool
            AudiobookMemoryPool.recycleMutableList(tempCategories)
        }
    }
    
    // Keep the old method for backward compatibility during migration
    private fun transformToGroupedCategories(audiobooks: List<AudiobookEntity>): ImmutableList<AudiobookCategory> {
        return transformToGroupedCategoriesOptimized(audiobooks)
    }
    
    override fun onCleared() {
        super.onCleared()
        // Clear memory pools to free up resources
        AudiobookMemoryPool.clearAllPools()
        Log.d("AudiobookViewModel", "Memory pools cleared on ViewModel destruction")
    }
}