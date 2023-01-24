package com.ondra.pdfmerge

import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ExpiryPolicyBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import java.time.Duration

@SpringBootApplication
@EnableCaching
class PdfmergeApplication {

    /**
     * @return A CacheManager with a preconfigured cache called "fileCache".
     * This cache can be used for storing up to ten elements (key: String, value: ByteArray) in the heap.
     * Elements stored in this cache are only available for ten minutes and are evicted afterwards.
     */
    @Bean
    fun getCacheManager(): CacheManager {
        val cacheConfigurationBuilder = newCacheConfigurationBuilder(String::class.java, ByteArray::class.java, ResourcePoolsBuilder.heap(10))
        val expiryPolicy = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(10))
        val cacheManager = CacheManagerBuilder.newCacheManagerBuilder().withCache("fileCache", cacheConfigurationBuilder.withExpiry(expiryPolicy)).build()
        cacheManager.init()
        return cacheManager
    }

}

fun main(args: Array<String>) {
    runApplication<PdfmergeApplication>(*args)
}
