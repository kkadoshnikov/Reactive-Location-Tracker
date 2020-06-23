package com.gmail.kadoshnikovkirill.locationtracker.controller

import com.gmail.kadoshnikovkirill.locationtracker.domain.Track
import com.gmail.kadoshnikovkirill.locationtracker.domain.TrackKey
import com.gmail.kadoshnikovkirill.locationtracker.dto.TrackDto
import com.gmail.kadoshnikovkirill.locationtracker.dto.UserCoordinatesDto
import com.gmail.kadoshnikovkirill.locationtracker.service.TracksService
import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.Timer
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.temporal.ChronoUnit

@RestController
@RequestMapping("/tracks")
class TracksController(private val tracksService: TracksService) {
    private val timer = Metrics.timer("track")

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @Timed("endpoints.track")
    fun track(@RequestBody dto: UserCoordinatesDto) {
        val sample = Timer.start()
        tracksService.track(dto.toEntity())
                .log()
                .subscribe { sample.stop(timer) }
    }

    @PostMapping(value = ["stream"], consumes = [MediaType.APPLICATION_STREAM_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    fun stream(@RequestBody dtoFlux: Flux<UserCoordinatesDto>): Mono<Void> {
        return dtoFlux
                .map { it.toEntity() }
                .flatMap { tracksService.track(it) }
                .log()
                .then()
    }

    @GetMapping(produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseBody
    fun findAll(
            @RequestParam("userId") userId: Long,
            @RequestParam(value = "period", defaultValue = "24") hours: Int): Flux<TrackDto> {
        return tracksService.findByUserIdAndPeriod(userId, hours).map { it.toDto() }
    }

    private fun UserCoordinatesDto.toEntity() = Track(
            key = TrackKey(
                    hr = timestamp.truncatedTo(ChronoUnit.HOURS),
                    userId = userId,
                    ts = timestamp),
            lat = lat,
            lon = lon
    )

    private fun Track.toDto() = TrackDto(
        userId = key.userId,
        lat = lat,
        lon = lon,
        timestamp = key.ts,
        countryCode = countryCode,
        postalCode = postalCode,
        country = country,
        region = region,
        city = city,
        street = street,
        house = house
    )
}