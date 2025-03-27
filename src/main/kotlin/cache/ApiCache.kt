package cache

import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.runBlocking

class ApiCache<K, V> {
    private val cache = ConcurrentHashMap<K, V>()

    fun getOrFetch(key: K, fetcher: suspend () -> V): V {
        return cache.computeIfAbsent(key) { runBlocking { fetcher() } }
    }

    fun getCachedKeys(): Set<K> = cache.keys 

    fun getCachedValue(key: K): V? = cache[key] 
}
