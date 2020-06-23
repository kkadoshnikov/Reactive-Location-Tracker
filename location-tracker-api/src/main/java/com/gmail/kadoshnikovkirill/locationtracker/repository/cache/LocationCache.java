package com.gmail.kadoshnikovkirill.locationtracker.repository.cache;

import com.gmail.kadoshnikovkirill.locationtracker.dto.LocationDto;
import reactor.core.publisher.Mono;

public interface LocationCache {

    Mono<LocationDto> get(float lat, float lon);

    void put(float lat, float lon, LocationDto dto);
}
