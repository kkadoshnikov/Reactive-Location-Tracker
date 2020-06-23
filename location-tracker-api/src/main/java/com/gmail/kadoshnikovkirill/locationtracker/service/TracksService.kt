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
    private val empty = LocationDto()

    fun track(coordinates: Track): Mono<Track> {
        return locationService
                .getLocationByCoordinates(coordinates.lat, coordinates.lon)
                .doOnError { e: Throwable? -> LOG.warn("Getting location by coordinates is failed. Coordinates: {}.", coordinates, e) }
                .onErrorReturn(empty)
                .flatMap { dto: LocationDto ->
                    val track = coordinates.copy(
                            countryCode = dto.countryCode,
                            postalCode = dto.postalCode,
                            country = dto.country,
                            region = dto.region,
                            city = dto.city,
                            street = dto.street,
                            house = dto.house)
                    repository.save(track)
                            .timeout(Duration.ofMillis(cassandraTimeout.toLong()))
                            .doOnError { e: Throwable? -> LOG.warn("Saving track in cassandra is failed. Track: {}.", track, e) }
                }
    }

    fun findByUserIdAndPeriod(userId: Long, periodInHours: Int): Flux<Track> {
        return repository.findByKeyUserIdAndPeriod(userId, periodInHours)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(TracksService::class.java)
    }
}