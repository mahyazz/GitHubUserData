package cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant

// Cache lifetime: 5 mins
class ApiCache<K, V>(private val expirationMillis: Long = 5 * 60 * 1000) { 
    private val cache = mutableMapOf<K, Pair<V, Instant>>()
    private val mutex = Mutex()

    suspend fun getOrFetch(key: K, fetcher: suspend () -> V): V {
        mutex.withLock {
            val cachedData = cache[key]
            if (cachedData != null && Instant.now().isBefore(cachedData.second.plusMillis(expirationMillis))) {
                return cachedData.first
            }

            val value = fetcher() 
            cache[key] = value to Instant.now() 
            return value
        }
    }

    fun getCachedKeys(): Set<K> = cache.keys

    fun getCachedValue(key: K): V? = cache[key]?.takeIf {
        Instant.now().isBefore(it.second.plusMillis(expirationMillis))
    }?.first
}
