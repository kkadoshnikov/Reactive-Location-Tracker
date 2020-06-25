package com.gmail.kadoshnikovkirill.locationtracker.controller

import com.gmail.kadoshnikovkirill.locationtracker.domain.Track
import com.gmail.kadoshnikovkirill.locationtracker.dto.TrackDTO
import com.gmail.kadoshnikovkirill.locationtracker.dto.UserCoordinatesDto
import com.gmail.kadoshnikovkirill.locationtracker.mapper.EntityDTOMapper
import com.gmail.kadoshnikovkirill.locationtracker.service.TracksService
import io.micrometer.core.instrument.Metrics
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/tracks")
class TracksController(
        private val tracksService: TracksService,
        private val userCoordinatesDTOMapper: EntityDTOMapper<Track, UserCoordinatesDto>,
        private val trackDTOMapper: EntityDTOMapper<Track, TrackDTO>
) {
    private val timer = Metrics.timer("track")

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun track(@RequestBody dto: UserCoordinatesDto): HttpEntity<*> {
        val track = userCoordinatesDTOMapper.toEntity(dto)
        tracksService.track(track)
                .log()
        return ResponseEntity.EMPTY
    }

    @PostMapping(value = ["stream"], consumes = [MediaType.APPLICATION_STREAM_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    fun stream(@RequestBody dtoFlux: Flux<UserCoordinatesDto>): Mono<Void> {
        return dtoFlux
                .map(userCoordinatesDTOMapper::toEntity)
                .flatMap(tracksService::track)
                .log()
                .then()
    }

    @GetMapping(produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseBody
    fun findAll(
            @RequestParam("userId") userId: Long,
            @RequestParam(value = "period", defaultValue = "24") hours: Int): Flux<TrackDTO> {
        return tracksService.findByUserIdAndPeriod(userId, hours)
                .map(trackDTOMapper::toDTO)
    }
}