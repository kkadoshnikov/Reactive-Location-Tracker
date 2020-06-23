package com.gmail.kadoshnikovkirill.locationtracker.service;

import com.gmail.kadoshnikovkirill.locationtracker.domain.Track;
import com.gmail.kadoshnikovkirill.locationtracker.repository.TracksRepository;
import com.gmail.kadoshnikovkirill.locationtracker.dto.LocationDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TracksService {

    private static final Logger LOG = LoggerFactory.getLogger(TracksService.class);

    private final TracksRepository repository;
    private final LocationService locationService;
    private final LocationDto empty = new LocationDto();
    @Value("${cassandraTimeout}")
    private Integer cassandraTimeout;

    public Mono<Track> track(Track coordinates) {
        return locationService
                .getLocationByCoordinates(coordinates.getLat(), coordinates.getLon())
                .doOnError(e -> LOG.warn("Getting location by coordinates is failed. Coordinates: {}.", coordinates, e))
                .onErrorReturn(empty)
                .flatMap(locationDto -> {
                    Track track = coordinates.toBuilder()
                            .countryCode(locationDto.getCountryCode())
                            .postalCode(locationDto.getPostalCode())
                            .country(locationDto.getCountry())
                            .region(locationDto.getRegion())
                            .city(locationDto.getCity())
                            .street(locationDto.getStreet())
                            .house(locationDto.getHouse())
                            .build();
                    return repository.save(track)
                            .timeout(Duration.ofMillis(cassandraTimeout))
                            .doOnError(e -> LOG.warn("Saving track in cassandra is failed. Track: {}.", track, e));
                });
    }

    public Flux<Track> findByUserIdAndPeriod(Long userId, Integer periodInHours) {
        return repository.findByKeyUserIdAndPeriod(userId, periodInHours);
    }
}
