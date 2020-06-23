package com.gmail.kadoshnikovkirill.locationtracker.controller;

import com.gmail.kadoshnikovkirill.locationtracker.domain.Track;
import com.gmail.kadoshnikovkirill.locationtracker.service.TracksService;
import com.gmail.kadoshnikovkirill.locationtracker.domain.TrackKey;
import com.gmail.kadoshnikovkirill.locationtracker.dto.TrackDto;
import com.gmail.kadoshnikovkirill.locationtracker.dto.UserCoordinatesDto;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.time.temporal.ChronoUnit.HOURS;

@RestController
@RequestMapping("/tracks")
@RequiredArgsConstructor
public class TracksController {

    private final TracksService tracksService;
    private Timer timer = Metrics.timer("track");

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @Timed("endpoints.track")
    public void track(@RequestBody UserCoordinatesDto dto) {
        Timer.Sample sample = Timer.start();
        tracksService.track(dtoToEntity(dto))
                .log()
                .subscribe(track -> sample.stop(timer));
    }

    @PostMapping(value = "stream", consumes = MediaType.APPLICATION_STREAM_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Mono<Void> stream(@RequestBody Flux<UserCoordinatesDto> dtoFlux) {
        return dtoFlux.map(this::dtoToEntity).flatMap(tracksService::track).log().then();
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<TrackDto> findAll(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "period", defaultValue = "24") Integer hours) {
        return tracksService.findByUserIdAndPeriod(userId, hours)
                .map(this::entityToDto);
    }

    private Track dtoToEntity(UserCoordinatesDto dto) {
        return Track.builder()
                .key(new TrackKey(dto.getTimestamp().truncatedTo(HOURS), dto.getUserId(), dto.getTimestamp()))
                .lat(dto.getLat())
                .lon(dto.getLon())
                .build();
    }

    private TrackDto entityToDto(Track track) {
        return TrackDto.builder()
                .userId(track.getKey().getUserId())
                .lat(track.getLat())
                .lon(track.getLon())
                .timestamp(track.getKey().getTs())
                .countryCode(track.getCountryCode())
                .postalCode(track.getPostalCode())
                .country(track.getCountry())
                .region(track.getRegion())
                .city(track.getCity())
                .street(track.getStreet())
                .house(track.getHouse())
                .build();
    }
}
