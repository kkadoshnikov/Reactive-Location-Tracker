package com.gmail.kadoshnikovkirill.locationtracker.provider;

import com.gmail.kadoshnikovkirill.locationtracker.dto.LocationDto;
import reactor.core.publisher.Mono;

public interface LocationProvider {

    Mono<LocationDto> getByCoordinates(float lat, float lon);
}
