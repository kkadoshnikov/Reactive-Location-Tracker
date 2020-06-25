package com.gmail.kadoshnikovkirill.locationtracker.repository.cache

import com.gmail.kadoshnikovkirill.locationtracker.dto.LocationDto
import reactor.core.publisher.Mono

interface LocationCache {
    operator fun get(lat: Float, lon: Float): Mono<LocationDto>
    operator fun set(lat: Float, lon: Float, dto: LocationDto)
}