package com.gmail.kadoshnikovkirill.locationtracker.service

import com.gmail.kadoshnikovkirill.locationtracker.domain.Track
import com.gmail.kadoshnikovkirill.locationtracker.dto.LocationDto
import com.gmail.kadoshnikovkirill.locationtracker.repository.TracksRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class TracksService(
        private val repository: TracksRepository,
        private val locationService: LocationService,
        @Value("\${cassandraTimeout}")
        private val cassandraTimeout: Int
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val empty = LocationDto()

    fun track(coordinates: Track): Mono<Track> {
        return locationService
                .getLocationByCoordinates(coordinates.lat, coordinates.lon)
                .doOnError { log.warn("Getting location by coordinates is failed. Coordinates: $coordinates.", it) }
                .onErrorReturn(empty)
                .map { buildTrack(coordinates, it) }
                .flatMap(::saveTrack)
    }

    fun findByUserIdAndPeriod(userId: Long, periodInHours: Int): Flux<Track> {
        return repository.findByKeyUserIdAndPeriod(userId, periodInHours)
    }

    private fun buildTrack(coordinates: Track, dto: LocationDto) = coordinates.copy(
            countryCode = dto.countryCode,
            postalCode = dto.postalCode,
            country = dto.country,
            region = dto.region,
            city = dto.city,
            street = dto.street,
            house = dto.house)

    private fun saveTrack(track: Track): Mono<Track> {
        return repository.save(track)
                .timeout(Duration.ofMillis(cassandraTimeout.toLong()))
                .doOnError { log.warn("Saving track in cassandra is failed. Track: $track.", it) }
    }
}