package com.gmail.kadoshnikovkirill.locationtracker.repository.cache

import com.gmail.kadoshnikovkirill.locationtracker.dto.LocationDto
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class RedisLocationCache(connectionFactory: ReactiveRedisConnectionFactory)
    : ReactiveRedisTemplate<String, LocationDto>(connectionFactory, context()), LocationCache {
    override fun get(lat: Float, lon: Float): Mono<LocationDto> {
        return this.opsForHash<String, LocationDto>()["location", hashKey(lat, lon)]
    }

    override fun set(lat: Float, lon: Float, dto: LocationDto) {
        this.opsForHash<Any, Any>().put("location", hashKey(lat, lon), dto).subscribe()
    }

    private fun hashKey(lat: Float, lon: Float): String {
        return "$lat:$lon"
    }

    companion object {
        private fun context(): RedisSerializationContext<String, LocationDto> {
            return RedisSerializationContext
                    .newSerializationContext<String, LocationDto>(StringRedisSerializer())
                    .hashKey(StringRedisSerializer())
                    .hashValue(KryoRedisSerializer<LocationDto>())
                    .build()
        }
    }
}