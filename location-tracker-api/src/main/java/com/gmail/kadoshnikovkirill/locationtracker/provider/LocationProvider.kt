package com.gmail.kadoshnikovkirill.locationtracker.provider

import com.gmail.kadoshnikovkirill.locationtracker.dto.LocationDto
import reactor.core.publisher.Mono

interface LocationProvider {
    fun getByCoordinates(lat: Float, lon: Float): Mono<LocationDto>
}