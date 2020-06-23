package com.gmail.kadoshnikovkirill.locationtracker.service;

import com.gmail.kadoshnikovkirill.locationtracker.dto.LocationDto;
import com.gmail.kadoshnikovkirill.locationtracker.provider.LocationProvider;
import com.gmail.kadoshnikovkirill.locationtracker.repository.cache.RedisLocationCache;
import com.gmail.kadoshnikovkirill.reactive.metrics.MeteredMono;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

// https://tech.yandex.com/maps/geocoder/doc/desc/concepts/input_params-docpage/
@Service
@RequiredArgsConstructor
public class LocationService {

    public static void main(String[] args) {
        Mono.just(2)
                .doOnEach(signal -> System.out.println(1 + " " + signal.getType()))
                .flatMap(i -> Mono.empty())
                .doOnEach(signal -> System.out.println(2 + " " + signal.getType()))
                .flatMap(i -> Mono.just(3)
                        .doOnEach(signal -> System.out.println(3 + " " + signal.getType()))
                ).subscribe();
    }

    private final RedisLocationCache locationCache;
    private final LocationProvider locationProvider;

    @MeteredMono(value = "location.get", percentiles = {0.75, 0.95, 0.98, 0.99, 0.999}, histogram = true)
    public Mono<LocationDto> getLocationByCoordinates(Float lat, Float lon) {
        float normalizedLat = normalize(lat);
        float normalizedLon = normalize(lon);
        return locationCache.get(normalizedLat, normalizedLon)
            .switchIfEmpty(locationProvider
                .getByCoordinates(normalizedLat, normalizedLon)
                .doOnNext(locationDto -> locationCache.put(normalizedLat, normalizedLon, locationDto)));
    }

    private float normalize(float number) {
        return Math.round(number * 10000f) / 10000f + 0.00005f;
    }
}
