package no.nav.eux.pdf.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

const val defaultRinaCpiSessionMinutes: Long = 30

@Configuration
@EnableCaching
class CacheConfig {
    companion object {
        const val CPI_SESSION_CACHE: String = "CPI_SESSION_CACHE"
    }

    @Bean
    fun cacheManager(): CacheManager {
        val cacheManager = SimpleCacheManager()
        cacheManager.setCaches(
            listOf<Cache>(
                CaffeineCache(
                    CPI_SESSION_CACHE, Caffeine.newBuilder()
                        .expireAfterWrite(defaultRinaCpiSessionMinutes - 1, TimeUnit.MINUTES)
                        .recordStats()
                        .build()
                )
            )
        )

        return cacheManager
    }
}
