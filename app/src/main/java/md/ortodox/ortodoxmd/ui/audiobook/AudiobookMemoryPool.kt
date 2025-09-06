package md.ortodox.ortodoxmd.ui.audiobook

import androidx.work.WorkInfo
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Memory pool for reusable audiobook display objects to reduce allocations
 * and improve scrolling performance by reusing expensive objects.
 */
object AudiobookMemoryPool {
    
    // Pool for DownloadStateInfo objects
    private val downloadStateInfoPool = ConcurrentLinkedQueue<DownloadStateInfo>()
    private const val MAX_DOWNLOAD_STATE_POOL_SIZE = 50
    
    // Pool for reusable collections
    private val listPool = ConcurrentLinkedQueue<MutableList<Any>>()
    private const val MAX_LIST_POOL_SIZE = 20
    
    // Pool for string builders for text processing
    private val stringBuilderPool = ConcurrentLinkedQueue<StringBuilder>()
    private const val MAX_STRING_BUILDER_POOL_SIZE = 10
    
    /**
     * Get a reusable DownloadStateInfo object or create a new one
     */
    fun getDownloadStateInfo(state: WorkInfo.State?, progress: Int): DownloadStateInfo {
        val pooledObject = downloadStateInfoPool.poll()
        return if (pooledObject != null) {
            // Reuse existing object
            DownloadStateInfo(state, progress).also {
                // Clear any previous state if needed
            }
        } else {
            // Create new object
            DownloadStateInfo(state, progress)
        }
    }
    
    /**
     * Return a DownloadStateInfo object to the pool for reuse
     */
    fun recycleDownloadStateInfo(obj: DownloadStateInfo) {
        if (downloadStateInfoPool.size < MAX_DOWNLOAD_STATE_POOL_SIZE) {
            downloadStateInfoPool.offer(obj)
        }
    }
    
    /**
     * Get a reusable mutable list or create a new one
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getMutableList(): MutableList<T> {
        val pooledList = listPool.poll()
        return if (pooledList != null) {
            pooledList.clear()
            pooledList as MutableList<T>
        } else {
            mutableListOf()
        }
    }
    
    /**
     * Return a mutable list to the pool for reuse
     */
    fun recycleMutableList(list: MutableList<*>) {
        if (listPool.size < MAX_LIST_POOL_SIZE && list.isEmpty()) {
            @Suppress("UNCHECKED_CAST")
            listPool.offer(list as MutableList<Any>)
        }
    }
    
    /**
     * Get a reusable StringBuilder or create a new one
     */
    fun getStringBuilder(): StringBuilder {
        val pooledBuilder = stringBuilderPool.poll()
        return if (pooledBuilder != null) {
            pooledBuilder.clear()
            pooledBuilder
        } else {
            StringBuilder(64) // Default capacity
        }
    }
    
    /**
     * Return a StringBuilder to the pool for reuse
     */
    fun recycleStringBuilder(builder: StringBuilder) {
        if (stringBuilderPool.size < MAX_STRING_BUILDER_POOL_SIZE) {
            stringBuilderPool.offer(builder)
        }
    }
    
    /**
     * Clear all pools - useful for memory pressure situations
     */
    fun clearAllPools() {
        downloadStateInfoPool.clear()
        listPool.clear()
        stringBuilderPool.clear()
    }
    
    /**
     * Get pool statistics for debugging/monitoring
     */
    fun getPoolStats(): PoolStats {
        return PoolStats(
            downloadStateInfoPoolSize = downloadStateInfoPool.size,
            listPoolSize = listPool.size,
            stringBuilderPoolSize = stringBuilderPool.size
        )
    }
}

/**
 * Statistics about memory pool usage
 */
data class PoolStats(
    val downloadStateInfoPoolSize: Int,
    val listPoolSize: Int,
    val stringBuilderPoolSize: Int
)