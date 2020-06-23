package com.gmail.kadoshnikovkirill.locationtracker.repository.cache;

import com.gmail.kadoshnikovkirill.locationtracker.dto.LocationDto;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RedisLocationCache extends ReactiveRedisTemplate<String, LocationDto> implements LocationCache {

    public RedisLocationCache(ReactiveRedisConnectionFactory connectionFactory) {
        super(connectionFactory, context());
    }

    private static RedisSerializationContext<String, LocationDto> context() {
        return RedisSerializationContext
                .<String, LocationDto>newSerializationContext(new StringRedisSerializer())
                .hashKey(new StringRedisSerializer())
                .hashValue(new KryoRedisSerializer<LocationDto>())
                .build();
    }

    @Override
    public Mono<LocationDto> get(float lat, float lon) {
        return this.<String, LocationDto>opsForHash().get("location", hashKey(lat, lon));
    }

    @Override
    public void put(float lat, float lon, LocationDto dto) {
        this.opsForHash().put("location", hashKey(lat, lon), dto).subscribe();
    }

    private String hashKey(float lat, float lon) {
        return lat + ":" + lon;
    }
}
