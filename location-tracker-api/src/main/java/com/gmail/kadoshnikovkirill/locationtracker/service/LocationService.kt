package com.gmail.kadoshnikovkirill.locationtracker.service

import com.gmail.kadoshnikovkirill.locationtracker.dto.LocationDto
import com.gmail.kadoshnikovkirill.locationtracker.provider.LocationProvider
import com.gmail.kadoshnikovkirill.locationtracker.repository.cache.LocationCache
import com.gmail.kadoshnikovkirill.reactive.metrics.MeteredMono
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import kotlin.math.roundToInt

@Service
class LocationService(
        private val locationCache: LocationCache,
        private val locationProvider: LocationProvider
) {

    @MeteredMono(value = "location.get", percentiles = [0.75, 0.95, 0.98, 0.99, 0.999], histogram = true)
    fun getLocationByCoordinates(lat: Float, lon: Float): Mono<LocationDto> {
        val normalizedLat = normalize(lat)
        val normalizedLon = normalize(lon)
        return locationCache[normalizedLat, normalizedLon]
                .switchIfEmpty(locationProvider
                        .getByCoordinates(normalizedLat, normalizedLon)
                        .doOnNext { locationCache[normalizedLat, normalizedLon] = it })
    }

    private fun normalize(number: Float): Float {
        return (number * 10000f).roundToInt() / 10000f + 0.00005f
    }
}