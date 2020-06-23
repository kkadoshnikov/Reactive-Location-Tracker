package com.gmail.kadoshnikovkirill.locationtracker.service

import com.gmail.kadoshnikovkirill.locationtracker.dto.LocationDto
import com.gmail.kadoshnikovkirill.locationtracker.provider.LocationProvider
import com.gmail.kadoshnikovkirill.locationtracker.repository.cache.RedisLocationCache
import com.gmail.kadoshnikovkirill.reactive.metrics.MeteredMono
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import kotlin.math.roundToInt

// https://tech.yandex.com/maps/geocoder/doc/desc/concepts/input_params-docpage/
@Service
class LocationService(
        private val locationCache: RedisLocationCache,
        private val locationProvider: LocationProvider
) {

    @MeteredMono(value = "location.get", percentiles = [0.75, 0.95, 0.98, 0.99, 0.999], histogram = true)
    fun getLocationByCoordinates(lat: Float, lon: Float): Mono<LocationDto> {
        val normalizedLat = normalize(lat)
        val normalizedLon = normalize(lon)
        return locationCache[normalizedLat, normalizedLon]
                .switchIfEmpty(locationProvider
                        .getByCoordinates(normalizedLat, normalizedLon)
                        .doOnNext { locationDto: LocationDto -> locationCache.put(normalizedLat, normalizedLon, locationDto) })
    }

    private fun normalize(number: Float): Float {
        return (number * 10000f).roundToInt() / 10000f + 0.00005f
    }
}